/**
 * A simple class that monitors the data and existence of a ZooKeeper
 * node. It uses asynchronous ZooKeeper APIs.
 */
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.Arrays;

public class DataMonitor implements Watcher, StatCallback {

    ZooKeeper zk;

    String znode;

    Watcher chainedWatcher;

    boolean dead;

    DataMonitorListener listener;

    byte prevData[];

    int prevChildNum;
    Stat stats;

    public DataMonitor(ZooKeeper zk, String znode, Watcher chainedWatcher,
                       DataMonitorListener listener) {
        this.zk = zk;
        this.znode = znode;
        this.chainedWatcher = chainedWatcher;
        this.listener = listener;
        stats =new Stat();
        // Get things started by checking if the node exists. We are going
        // to be completely event driven
        zk.exists(znode, true, this, null);
        try {
            zk.getChildren(znode, new ChildWather(), stats);
        } catch (KeeperException.NoNodeException e) {
            System.out.println("Wait for /znode_testowy to start");
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        prevChildNum = 0;
    }

    public void process(WatchedEvent event) {
        String path = event.getPath();
        if (event.getType() == Event.EventType.None) {
            // We are are being told that the state of the
            // connection has changed
            switch (event.getState()) {
                case SyncConnected:
                    // In this particular example we don't need to do anything
                    // here - watches are automatically re-registered with
                    // server and any watches triggered while the client was
                    // disconnected will be delivered (in order of course)
                    break;
                case Expired:
                    // It's all over
                    dead = true;
                    listener.closing(KeeperException.Code.SessionExpired);
                    break;

            }
        }else {
            if (path != null && path.equals(znode)) {
                // Something has changed on the node, let's find out
                zk.exists(znode, true, this, null);
            }
        }
        if (chainedWatcher != null) {
            chainedWatcher.process(event);
        }
    }

    public void processResult(int rc, String path, Object ctx, Stat stat) {


        boolean exists;
        switch (rc) {
            case Code.Ok:
                exists = true;
                break;
            case Code.NoNode:
                exists = false;
                break;
            case Code.SessionExpired:
            case Code.NoAuth:
                dead = true;
                listener.closing(rc);
                return;
            default:
                // Retry errors
                zk.exists(znode, true, this, null);
                return;
        }

        byte b[] = null;

        if (exists) {
            try {
                b = zk.getData(znode, false, null);
                System.out.println(stat.getNumChildren());
            } catch (KeeperException e) {
                // We don't need to worry about recovering now. The watch
                // callbacks will kick off any exception handling
                e.printStackTrace();
            } catch (InterruptedException e) {
                return;
            }
        }


        if ((b == null && b != prevData)
                || (b != null && !Arrays.equals(prevData, b))  ) {

            listener.exists(b);
            prevData = b;

        }

    }


    class ChildWather implements Watcher{
        public void process(WatchedEvent e) {
            System.out.println("Processing event: " + e.toString());

            if(e.getType() == Event.EventType.NodeDeleted){
                return;
            }

            try {
                zk.getChildren(znode, this,stats);
                System.out.println("Children count from children watcher: " + stats.getNumChildren());
            }  catch (KeeperException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            if (e.getType() == Event.EventType.NodeCreated) {
                System.out.println("Child created!");
            }
        }
    }
}
