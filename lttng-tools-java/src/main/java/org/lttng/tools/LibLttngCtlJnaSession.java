package org.lttng.tools;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.Union;

class LibLttngCtlJnaSession implements ILttngSession {

    private static interface LttngCtlLibrary extends Library {
        LttngCtlLibrary INSTANCE = (LttngCtlLibrary) Native.loadLibrary("lttng-ctl", LttngCtlLibrary.class);

        public static class lttng_domain extends Structure {
            public int type; /* 3 = JUL, 4 = Log4j */
            public int buf_type; /* 0 = per_pid, 1 = per_uid */
            public String padding;

            public attr_union attr;
            public static class attr_union extends Union {
                public int pid;
                public String exec_name;
                public String padding;
            }

            @Override
            protected List getFieldOrder() {
                return Arrays.asList("type", "buf_type", "padding", "attr");
            }
        }

        public static class lttng_handle extends Structure {
            public String session_name;
            public lttng_domain domain;
            public String padding;

            @Override
            protected List getFieldOrder() {
                return Arrays.asList("session_name", "domain", "padding");
            }
        }

        int lttng_create_session(String name, String url);
        int lttng_destroy_session(String name);

        lttng_handle lttng_create_handle(String session_name, lttng_domain domain);
        void lttng_destroy_handle(lttng_handle handle);

        int lttng_list_domains(String session_name, lttng_domain[] domains);

//        int lttng_enable_event(lttng_handle handle, lttng_event ev, String channel_name);
    }


    public static void main(String[] args) {
        final String sessionName = "MySession";

        LttngCtlLibrary.INSTANCE.lttng_create_session(sessionName, null);

        LttngCtlLibrary.lttng_domain myDomain = new LttngCtlLibrary.lttng_domain();
        myDomain.type = 3;
        myDomain.buf_type = 1;
        LttngCtlLibrary.lttng_handle myHandle2 = new LttngCtlLibrary.lttng_handle();
        LttngCtlLibrary.lttng_handle myHandle = LttngCtlLibrary.INSTANCE.lttng_create_handle(sessionName, myDomain);

        LttngCtlLibrary.INSTANCE.lttng_destroy_handle(myHandle);

        LttngCtlLibrary.INSTANCE.lttng_destroy_session(sessionName);
    }

    private final String sessionName;
    private final Domain domain;

    public LibLttngCtlJnaSession(String sessionName, Domain domain) {
        if (sessionName != null) {
            this.sessionName = sessionName;
        } else {
            this.sessionName = UUID.randomUUID().toString();
        }
        this.domain = domain;
    }


    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean enableAllEvents() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean enableEvents(String... enabledEvents) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean disableEvents(String... disabledEvents) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> view() {
        // TODO Auto-generated method stub
        return null;
    }
}
