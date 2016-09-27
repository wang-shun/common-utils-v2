package com.youzan.sz.jutil.admin;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

//import com.qq.jutil.admin.AdminServer.CmdInfo;
//import com.qq.jutil.string.StringUtil;
import com.youzan.sz.jutil.admin.AdminServer.CmdInfo;
import com.youzan.sz.jutil.string.StringUtil;

final class HelpCommand implements AdminCommand {
    private static final int          LENGTH     = 20;
    private static final String       SPACE_FILL = getSpace(LENGTH);

    private List<CmdInfo>             cmdinfos;
    private Map<String, AdminCommand> cmds;

    public HelpCommand(List<CmdInfo> cmdinfos, Map<String, AdminCommand> cmds) {
        this.cmdinfos = cmdinfos;
        this.cmds = cmds;
    }

    private static String getSpace(int count) {
        if (count <= 0)
            return " ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; ++i)
            sb.append(" ");
        return sb.toString();
    }

    public void execute(String[] argv, PrintWriter out) {
        if (argv.length > 0) {
            String name = argv[0];
            AdminCommand c = cmds.get(name);
            if (c == null) {
                out.println("can not find command: " + name);
            } else {
                out.println(name);
                out.println("---------");
                out.println(c);
            }
            return;
        }
        out.println("admin commands: ");
        for (CmdInfo c : cmdinfos) {
            out.print(c.name);
            out.print(getSpace(LENGTH - c.name.length()));
            String[] lines = StringUtil.split(c.cmd.toString(), "\n");
            out.println(lines[0]);
            for (int i = 1; i < lines.length; ++i) {
                out.print(SPACE_FILL);
                out.println(lines[i]);
            }
        }
    }

    public String toString() {
        return "print admin command help.\nusage:\n\thelp [cmd]";
    }
}
