
/**
 * A simple example program to use DataMonitor to start and
 * stop executables based on a znode. The program watches the
 * specified znode and saves the data that corresponds to the
 * znode in the filesystem. It also starts the specified program
 * with the specified arguments when the znode exists and kills
 * the program if the znode goes away.
 */

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class Executor
        implements Watcher, Runnable, DataMonitorListener {

    private DataMonitor dm;

    private ZooKeeper zk;


    private final String[] exec;

    private Process child;

    private List<String> children = null;

    private static final String path = "/znode_testowy";


    private Executor(String host, String znode,
                     String exec[]) throws IOException {
        this.exec = exec;
        zk = new ZooKeeper(host, 2181, this);
        dm = new DataMonitor(zk, znode, null, this);

    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err
                    .println("USAGE: program [args ...]");
            System.exit(2);
        }
        String host = "localhost";

        String exec[] = new String[args.length];
        System.arraycopy(args, 0, exec, 0, exec.length);
        try {
            new Executor(host, path, exec).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***************************************************************************
     * We do process any events ourselves, we just need to forward them on.
     *
     * @see org.apache.zookeeper.Watcher process(org.apache.zookeeper.proto.WatcherEvent)
     */
    public void process(WatchedEvent event) {
        dm.process(event);
    }

    public void run() {
        try {
            synchronized (this) {
                while (!dm.dead) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
        }
    }

    public void closing(int rc) {
        synchronized (this) {
            notifyAll();
        }
    }

    static class StreamWriter extends Thread {
        final OutputStream os;

        final InputStream is;

        StreamWriter(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
            start();
        }

        public void run() {
            byte b[] = new byte[80];
            int rc;
            try {
                while ((rc = is.read(b)) > 0) {
                    os.write(b, 0, rc);
                }
            } catch (IOException e) {
            }

        }
    }

    public void exists(byte[] data) {
        if (data == null) {
            if (child != null) {
                System.out.println("Killing process");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                }
            }
            child = null;
        } else try {

            if (new String(data, "UTF-8").startsWith("print")) {
                System.out.println("Print children");
                printChildren(path, path, "");

            } else {

                if (child != null) {
                    System.out.println("Stopping child");
                    child.destroy();
                    try {
                        child.waitFor();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    System.out.println("Starting child");
                    child = Runtime.getRuntime().exec(exec);
                    new StreamWriter(child.getInputStream(), System.out);
                    new StreamWriter(child.getErrorStream(), System.err);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    private void printChildren(String name, String curr_path, String format){
        System.out.println(format + name);

        try {
            children = zk.getChildren(curr_path, false);
            if(children.size() == 0)
                return;

            for (String child_name: children){
                printChildren(child_name, curr_path + "/"+ child_name, "\t"+format);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
