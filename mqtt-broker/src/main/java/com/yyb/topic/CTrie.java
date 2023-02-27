package com.yyb.topic;

import com.yyb.bean.SubscribeBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class CTrie {

    private enum Action {
        OK, REPEAT
    }

    private static final Token ROOT = new Token("root");

    private static final INode NO_PARENT = null;

    private INode root;

    CTrie() {
        final CNode mainNode = new CNode();
        mainNode.token = ROOT;
        this.root = new INode(mainNode);
    }

    enum NavigationAction {
        MATCH, GODEEP, STOP
    }

    private NavigationAction evaluate(Topic topic, CNode cnode) {
        if (Token.MULTI.equals(cnode.token)) {
            return NavigationAction.MATCH;
        }
        if (topic.isEmpty()) {
            return NavigationAction.STOP;
        }
        final Token token = topic.headToken();
        if (!(Token.SINGLE.equals(cnode.token) || cnode.token.equals(token) || ROOT.equals(cnode.token))) {
            return NavigationAction.STOP;
        }
        return NavigationAction.GODEEP;
    }

    public Set<SubscribeBO> recursiveMatch(Topic topic) {
        return recursiveMatch(topic, this.root);
    }

    private Set<SubscribeBO> recursiveMatch(Topic topic, INode inode) {
        CNode cnode = inode.mainNode();
        NavigationAction action = evaluate(topic, cnode);
        if (action == NavigationAction.MATCH) {
            return cnode.subscriptions;
        }
        if (action == NavigationAction.STOP) {
            return Collections.emptySet();
        }
        if (cnode instanceof TNode) {
            return Collections.emptySet();
        }
        Topic remainingTopic = (ROOT.equals(cnode.token)) ? topic : topic.exceptHeadToken();
        Set<SubscribeBO> subscriptions = new HashSet<>();
        if (remainingTopic.isEmpty()) {
            subscriptions.addAll(cnode.subscriptions);
        }
        for (INode subInode : cnode.allChildren()) {
            subscriptions.addAll(recursiveMatch(remainingTopic, subInode));
        }
        return subscriptions;
    }

    public void addToTree(SubscribeBO newSubscription) {
        Action res;
        do {
            res = insert(newSubscription.getTopicFilter(), this.root, newSubscription);
        } while (res == Action.REPEAT);
    }

    private Action insert(Topic topic, final INode inode, SubscribeBO newSubscription) {
        Token token = topic.headToken();
        if (!topic.isEmpty() && inode.mainNode().anyChildrenMatch(token)) {
            Topic remainingTopic = topic.exceptHeadToken();
            INode nextInode = inode.mainNode().childOf(token);
            return insert(remainingTopic, nextInode, newSubscription);
        } else {
            if (topic.isEmpty()) {
                return insertSubscription(inode, newSubscription);
            } else {
                return createNodeAndInsertSubscription(topic, inode, newSubscription);
            }
        }
    }

    private Action insertSubscription(INode inode, SubscribeBO newSubscription) {
        CNode cnode = inode.mainNode();
        CNode updatedCnode = cnode.copy().addSubscription(newSubscription);
        if (inode.compareAndSet(cnode, updatedCnode)) {
            return Action.OK;
        } else {
            return Action.REPEAT;
        }
    }

    private Action createNodeAndInsertSubscription(Topic topic, INode inode, SubscribeBO newSubscription) {
        INode newInode = createPathRec(topic, newSubscription);
        CNode cnode = inode.mainNode();
        CNode updatedCnode = cnode.copy();
        updatedCnode.add(newInode);

        return inode.compareAndSet(cnode, updatedCnode) ? Action.OK : Action.REPEAT;
    }

    private INode createPathRec(Topic topic, SubscribeBO newSubscription) {
        Topic remainingTopic = topic.exceptHeadToken();
        if (!remainingTopic.isEmpty()) {
            INode inode = createPathRec(remainingTopic, newSubscription);
            CNode cnode = new CNode();
            cnode.token = topic.headToken();
            cnode.add(inode);
            return new INode(cnode);
        } else {
            return createLeafNodes(topic.headToken(), newSubscription);
        }
    }

    private INode createLeafNodes(Token token, SubscribeBO newSubscription) {
        CNode newLeafCnode = new CNode();
        newLeafCnode.token = token;
        newLeafCnode.addSubscription(newSubscription);

        return new INode(newLeafCnode);
    }

    public void removeFromTree(Topic topic, String clientID) {
        Action res;
        do {
            res = remove(clientID, topic, this.root, NO_PARENT);
        } while (res == Action.REPEAT);
    }

    private Action remove(String clientId, Topic topic, INode inode, INode iParent) {
        Token token = topic.headToken();
        if (!topic.isEmpty() && (inode.mainNode().anyChildrenMatch(token))) {
            Topic remainingTopic = topic.exceptHeadToken();
            INode nextInode = inode.mainNode().childOf(token);
            return remove(clientId, remainingTopic, nextInode, inode);
        } else {
            final CNode cnode = inode.mainNode();
            if (cnode instanceof TNode) {
                // this inode is a tomb, has no clients and should be cleaned up
                // Because we implemented cleanTomb below, this should be rare, but possible
                // Consider calling cleanTomb here too
                return Action.OK;
            }
            if (cnode.containsOnly(clientId) && topic.isEmpty() && cnode.allChildren().isEmpty()) {
                // last client to leave this node, AND there are no downstream children, remove via TNode tomb
                if (inode == this.root) {
                    return inode.compareAndSet(cnode, inode.mainNode().copy()) ? Action.OK : Action.REPEAT;
                }
                TNode tnode = new TNode();
                return inode.compareAndSet(cnode, tnode) ? cleanTomb(inode, iParent) : Action.REPEAT;
            } else if (cnode.contains(clientId) && topic.isEmpty()) {
                CNode updatedCnode = cnode.copy();
                updatedCnode.removeSubscriptionsFor(clientId);
                return inode.compareAndSet(cnode, updatedCnode) ? Action.OK : Action.REPEAT;
            } else {
                //someone else already removed
                return Action.OK;
            }
        }
    }

    /**
     *
     * Cleans Disposes of TNode in separate Atomic CAS operation per
     * http://bravenewgeek.com/breaking-and-entering-lose-the-lock-while-embracing-concurrency/
     *
     * We roughly follow this theory above, but we allow CNode with no Subscriptions to linger (for now).
     *
     *
     * @param inode inode that handle to the tomb node.
     * @param iParent inode parent.
     * @return REPEAT if the this methods wasn't successful or OK.
     */
    private Action cleanTomb(INode inode, INode iParent) {
        CNode updatedCnode = iParent.mainNode().copy();
        updatedCnode.remove(inode);
        return iParent.compareAndSet(iParent.mainNode(), updatedCnode) ? Action.OK : Action.REPEAT;
    }

}
