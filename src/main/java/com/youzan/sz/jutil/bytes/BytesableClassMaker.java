package com.youzan.sz.jutil.bytes;
//

//import static com.qq.jutil.bytes.Bytesable.BOOLEAN;
//import static com.qq.jutil.bytes.Bytesable.BOOLEAN_LIST;
//import static com.qq.jutil.bytes.Bytesable.BYTES;
//import static com.qq.jutil.bytes.Bytesable.BYTESABLE;
//import static com.qq.jutil.bytes.Bytesable.BYTES_LIST;
//import static com.qq.jutil.bytes.Bytesable.INTEGER;
//import static com.qq.jutil.bytes.Bytesable.INT_LIST;
//import static com.qq.jutil.bytes.Bytesable.LIST;
//import static com.qq.jutil.bytes.Bytesable.LONG;
//import static com.qq.jutil.bytes.Bytesable.LONG_LIST;
//import static com.qq.jutil.bytes.Bytesable.STRING_LIST;
//import static com.qq.jutil.bytes.Bytesable.STRING;
import com.youzan.sz.jutil.config.XMLConfigElement;
import com.youzan.sz.jutil.config.XMLConfigFile;

import static com.youzan.sz.jutil.bytes.Bytesable.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

//import com.qq.jutil.config.XMLConfigElement;
//import com.qq.jutil.config.XMLConfigFile;

public class BytesableClassMaker {
    public static String genClass(String packageName, String className, List<DateTypeDesc> desc) {
        StringBuffer sbHead = new StringBuffer();
        StringBuffer sb = new StringBuffer();
        StringBuffer sbParam = new StringBuffer();
        StringBuffer sbInit = new StringBuffer();
        StringBuffer sbToBytes = new StringBuffer();
        StringBuffer sbRecover = new StringBuffer();
        StringBuffer sbToString = new StringBuffer();

        sbHead.append("package ").append(packageName).append(";\n\n");
        sbHead.append("import java.io.*;\n");
        sbHead.append("import java.nio.ByteBuffer;\n");
        sbHead.append("import com.qq.jutil.bytes.ByteUtil;\n");
        sbHead.append("import com.qq.jutil.bytes.Bytesable;\n");
        sbHead.append("import com.qq.jutil.bytes.Debyter;\n");

        sb.append("@SuppressWarnings(\"serial\")\n");
        sb.append("public class ").append(className).append(" implements Bytesable\n");

        //---------sbToBytes
        sbToBytes.append("\n\tpublic byte[] toBytes()\n");
        sbToBytes.append("\t{\n");
        sbToBytes.append("\t\tByteArrayOutputStream bos = new ByteArrayOutputStream();\n");
        sbToBytes.append("\t\ttry\n");
        sbToBytes.append("\t\t{\n");

        //-----------sbRecover
        sbRecover.append("\n\tpublic " + className + "(byte[] bytes)\n");
        sbRecover.append("\t{\n");
        sbRecover.append("\t\tByteBuffer bb = ByteBuffer.wrap(bytes);\n");

        //-----------sbToString
        sbToString.append("\n\tpublic String toString()\n");
        sbToString.append("\t{\n");
        sbToString.append("\t\tStringBuilder sb = new StringBuilder();\n");
        sbToString.append("\t\tsb.append(\"[" + className + ": \");\n");

        sb.append("{\n");

        boolean hasImportList = false;
        for (DateTypeDesc field : desc) {
            switch (field.dateType) {
                case INTEGER:
                    //sb
                    sb.append("\tpublic int ").append(field.name).append(";\n");
                    //sbParam
                    sbParam.append("int ").append(field.name).append(", ");
                    //sbInit
                    sbInit.append("\t\tthis.").append(field.name).append(" = ").append(field.name).append(";\n");
                    //sbToBytes
                    sbToBytes.append("\t\t\tbos.write(ByteUtil.enbyteInt(" + field.name + "));\n");
                    //sbRecover
                    sbRecover.append("\t\tthis." + field.name + " = Debyter.getInt(bb);\n");

                    //sbToString
                    sbToString.append("\t\tsb.append(" + field.name + ").append(\", \");\n");

                    break;
                case LONG:
                    sb.append("\tpublic long ").append(field.name).append(";\n");
                    sbParam.append("long ").append(field.name).append(", ");
                    sbInit.append("\t\tthis.").append(field.name).append(" = ").append(field.name).append(";\n");
                    sbToBytes.append("\t\t\tbos.write(ByteUtil.enbyteLong(" + field.name + "));\n");

                    sbRecover.append("\t\tthis." + field.name + " = Debyter.getLong(bb);\n");

                    sbToString.append("\t\tsb.append(" + field.name + ").append(\", \");\n");

                    break;
                case BOOLEAN:
                    sb.append("\tpublic boolean ").append(field.name).append(";\n");
                    sbParam.append("boolean ").append(field.name).append(", ");
                    sbInit.append("\t\tthis.").append(field.name).append(" = ").append(field.name).append(";\n");
                    sbToBytes.append("\t\t\tbos.write(ByteUtil.enbyteBoolean(" + field.name + "));\n");

                    sbRecover.append("\t\tthis." + field.name + " = Debyter.getBoolean(bb);\n");

                    sbToString.append("\t\tsb.append(" + field.name + ").append(\", \");\n");

                    break;
                case BYTES:
                    sb.append("\tpublic byte[] ").append(field.name).append(";\n");
                    sbParam.append("byte[] ").append(field.name).append(", ");
                    sbInit.append("\t\tthis.").append(field.name).append(" = ").append(field.name).append(";\n");

                    sbToBytes.append("\t\t\tbos.write(ByteUtil.enbyteBytes(" + field.name + "));\n");

                    sbRecover.append("\t\tthis." + field.name + " = Debyter.getBytes(bb);\n");

                    sbToString.append("\t\tsb.append(" + field.name
                                      + " == null ? \"null\" : com.qq.jutil.crypto.HexUtil.bytes2HexStr(" + field.name
                                      + ")).append(\", \");\n");

                    break;
                case STRING:
                    sb.append("\tpublic String ").append(field.name).append(";\n");
                    sbParam.append("String ").append(field.name).append(", ");
                    sbInit.append("\t\tthis.").append(field.name).append(" = ").append(field.name).append(";\n");

                    sbToBytes.append("\t\t\tbos.write(ByteUtil.enbyteString(" + field.name + "));\n");

                    sbRecover.append("\t\tthis." + field.name + " = Debyter.getString(bb);\n");

                    sbToString.append(
                        "\t\tsb.append(" + field.name + " == null ? \"null\" : " + field.name + ").append(\", \");\n");
                    break;

                case BYTESABLE:
                    sbHead.append("import " + field.pkg + "." + field.className + ";\n");

                    sb.append("\tpublic " + field.className + " ").append(field.name).append(";\n");
                    sbParam.append("" + field.className + " ").append(field.name).append(", ");
                    sbInit.append("\t\tthis.").append(field.name).append(" = ").append(field.name).append(";\n");

                    sbToBytes.append("\t\t\tbos.write(ByteUtil.enbyteBytesable(" + field.name + "));\n");

                    sbRecover.append(
                        "\t\tthis." + field.name + " = Debyter.getBytesable(" + field.className + ".class,bb);\n");

                    sbToString.append(
                        "\t\tsb.append(" + field.name + " == null ? \"null\" : " + field.name + ").append(\", \");\n");
                    break;

                case LIST:
                    if (!hasImportList) {
                        sbHead.append("import java.util.*;\n");
                        hasImportList = true;
                    }
                    sbHead.append("import " + field.pkg + "." + field.className + ";\n");

                    sb.append("\tpublic List<" + field.className + "> ").append(field.name).append(";\n");
                    sbParam.append("List<" + field.className + "> ").append(field.name).append(", ");
                    sbInit.append("\t\tthis.").append(field.name).append(" = ").append(field.name).append(";\n");

                    sbToBytes.append("\t\t\tbos.write(ByteUtil.enbyteList(" + field.name + "));\n");

                    sbRecover.append(
                        "\t\tthis." + field.name + " = Debyter.getBytesableList(" + field.className + ".class,bb);\n");

                    sbToString.append(
                        "\t\tsb.append(" + field.name + " == null ? \"null\" : " + field.name + ").append(\", \");\n");
                    break;

                case INT_LIST:
                    if (!hasImportList) {
                        sbHead.append("import java.util.*;\n");
                        hasImportList = true;
                    }
                    sb.append("\tpublic List<Integer> ").append(field.name).append(";\n");
                    sbParam.append("List<Integer> ").append(field.name).append(", ");
                    sbInit.append("\t\tthis.").append(field.name).append(" = ").append(field.name).append(";\n");

                    sbToBytes.append("\t\t\tbos.write(ByteUtil.enbyteIntList(" + field.name + "));\n");

                    sbRecover.append("\t\tthis." + field.name + " = Debyter.getIntList(bb);\n");

                    sbToString.append(
                        "\t\tsb.append(" + field.name + " == null ? \"null\" : " + field.name + ").append(\", \");\n");
                    break;
                case LONG_LIST:
                    if (!hasImportList) {
                        sbHead.append("import java.util.*;\n");
                        hasImportList = true;
                    }
                    sb.append("\tpublic List<Long> ").append(field.name).append(";\n");
                    sbParam.append("List<Long> ").append(field.name).append(", ");
                    sbInit.append("\t\tthis.").append(field.name).append(" = ").append(field.name).append(";\n");

                    sbToBytes.append("\t\t\tbos.write(ByteUtil.enbyteLongList(" + field.name + "));\n");

                    sbRecover.append("\t\tthis." + field.name + " = Debyter.getLongList(bb);\n");

                    sbToString.append(
                        "\t\tsb.append(" + field.name + " == null ? \"null\" : " + field.name + ").append(\", \");\n");
                    break;
                case BOOLEAN_LIST:
                    if (!hasImportList) {
                        sbHead.append("import java.util.*;\n");
                        hasImportList = true;
                    }
                    sb.append("\tpublic List<Boolean> ").append(field.name).append(";\n");
                    sbParam.append("List<Boolean> ").append(field.name).append(", ");
                    sbInit.append("\t\tthis.").append(field.name).append(" = ").append(field.name).append(";\n");

                    sbToBytes.append("\t\t\tbos.write(ByteUtil.enbyteBooleanList(" + field.name + "));\n");

                    sbRecover.append("\t\tthis." + field.name + " = Debyter.getBooleanList(bb);\n");

                    sbToString.append(
                        "\t\tsb.append(" + field.name + " == null ? \"null\" : " + field.name + ").append(\", \");\n");
                    break;
                case BYTES_LIST:
                    if (!hasImportList) {
                        sbHead.append("import java.util.*;\n");
                        hasImportList = true;
                    }
                    sb.append("\tpublic List<byte[]> ").append(field.name).append(";\n");
                    sbParam.append("List<byte[]> ").append(field.name).append(", ");
                    sbInit.append("\t\tthis.").append(field.name).append(" = ").append(field.name).append(";\n");

                    sbToBytes.append("\t\t\tbos.write(ByteUtil.enbyteBytesList(" + field.name + "));\n");

                    sbRecover.append("\t\tthis." + field.name + " = Debyter.getBytesList(bb);\n");

                    sbToString.append(
                        "\t\tsb.append(" + field.name + " == null ? \"null\" : " + field.name + ").append(\", \");\n");
                    break;
                case STRING_LIST:
                    if (!hasImportList) {
                        sbHead.append("import java.util.*;\n");
                        hasImportList = true;
                    }
                    sb.append("\tpublic List<String> ").append(field.name).append(";\n");
                    sbParam.append("List<String> ").append(field.name).append(", ");
                    sbInit.append("\t\tthis.").append(field.name).append(" = ").append(field.name).append(";\n");

                    sbToBytes.append("\t\t\tbos.write(ByteUtil.enbyteStringList(" + field.name + "));\n");

                    sbRecover.append("\t\tthis." + field.name + " = Debyter.getStringList(bb);\n");

                    sbToString.append(
                        "\t\tsb.append(" + field.name + " == null ? \"null\" : " + field.name + ").append(\", \");\n");
                    break;
            }
        }
        if (sbParam.length() > 1)//删除最后一个,
        {
            sbParam.deleteCharAt(sbParam.length() - 1);//删空格
            sbParam.deleteCharAt(sbParam.length() - 1);//删,
        }
        if (sbToString.length() > 13)//删除最后一个.append(", ")
            sbToString.delete(sbToString.length() - 15, sbToString.length() - 2);

        sbToBytes.append("\t\t}\n");
        sbToBytes.append("\t\tcatch(IOException e)\n");
        sbToBytes.append("\t\t{\n");
        sbToBytes.append("\t\t\tthrow new RuntimeException(\"ToBytes fail.\"+ e.getMessage());\n");
        sbToBytes.append("\t\t}\n");
        sbToBytes.append("\t\treturn bos.toByteArray();\n");
        sbToBytes.append("\t}\n");

        //------sbRecover
        sbRecover.append("\t}\n");

        //sbToString		
        sbToString.append("\t\tsb.append(\"]\");\n");
        sbToString.append("\t\treturn sb.toString();\n");
        sbToString.append("\t}\n");

        sb.append("\n").append("\tpublic ").append(className).append("()");
        sb.append("\n").append("\t{\n");
        sb.append("\t}\n");
        sb.append("\n").append("\tpublic ").append(className);
        sb.append(" initFromBytes(byte[] bytes").append(")");
        sb.append("\n").append("\t{\n");
        sb.append("\t\t return new ").append(className).append("(bytes);\n");
        sb.append("\t}\n");
        sb.append(sbRecover);
        sb.append("\n").append("\tpublic ").append(className).append("(");
        sb.append(sbParam).append(")").append("\n").append("\t{\n");
        sb.append(sbInit);
        sb.append("\t}\n");

        sb.append(sbToBytes);
        sb.append(sbToString);
        sb.append("}");
        sb.insert(0, sbHead);
        return sb.toString();
    }

    public static void genClassFile(String descFile, String savePath) {
        try {
            XMLConfigFile xf = new XMLConfigFile();
            xf.parse(new FileInputStream(new File(descFile)));
            XMLConfigElement root = xf.getRootElement();
            ArrayList<XMLConfigElement> ls = root.getChildListByName("classdesc");
            for (int i = 0; i < ls.size(); ++i) {
                XMLConfigElement elClazz = ls.get(i);
                String pkg = elClazz.getStringAttribute("pkg", "");
                String className = elClazz.getStringAttribute("name", "");
                List<DateTypeDesc> fields = new ArrayList<DateTypeDesc>();
                ArrayList<XMLConfigElement> lsField = elClazz.getChildListByName("field");
                for (int j = 0; j < lsField.size(); ++j) {
                    XMLConfigElement elField = lsField.get(j);
                    String fieldName = elField.getStringAttribute("name", "");
                    String type = elField.getStringAttribute("type", "");
                    int dateType = getDataType(type);
                    String dataPkg = "";
                    String dataClassName = "";
                    if (dateType == BYTESABLE || dateType == LIST) {
                        XMLConfigElement elDataPkg = elField.getChildByName("class");
                        dataPkg = elDataPkg.getStringAttribute("pkg", "");
                        dataClassName = elDataPkg.getStringAttribute("name", "");
                    }

                    fields.add(new DateTypeDesc(fieldName, getDataType(type), dataPkg, dataClassName));
                }
                String filePath = savePath + "/" + pkg.replaceAll("\\.", "/");
                File file = new File(filePath);
                file.mkdirs();
                FileOutputStream fos = new FileOutputStream(filePath + "/" + className + ".java");
                fos.write(genClass(pkg, className, fields).getBytes());
                fos.close();
                System.out.println("generate class " + className + " success.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getDataType(String name) {
        if (name.equalsIgnoreCase("INT") || name.equalsIgnoreCase("INTEGER"))
            return INTEGER;
        if (name.equalsIgnoreCase("LONG"))
            return LONG;
        if (name.equalsIgnoreCase("BOOLEAN"))
            return BOOLEAN;
        if (name.equalsIgnoreCase("BYTES"))
            return BYTES;
        if (name.equalsIgnoreCase("STRING"))
            return STRING;
        if (name.equalsIgnoreCase("BYTESABLE"))
            return BYTESABLE;
        if (name.equalsIgnoreCase("INT_LIST"))
            return INT_LIST;
        if (name.equalsIgnoreCase("LONG_LIST"))
            return LONG_LIST;
        if (name.equalsIgnoreCase("BOOLEAN_LIST"))
            return BOOLEAN_LIST;
        if (name.equalsIgnoreCase("BYTES_LIST"))
            return BYTES_LIST;
        if (name.equalsIgnoreCase("STRING_LIST"))
            return STRING_LIST;
        if (name.equalsIgnoreCase("LIST"))
            return LIST;
        return 0;
    }

    public static class DateTypeDesc {
        public String name;
        public int    dateType;
        public String pkg;
        public String className;

        public DateTypeDesc(String name, int dateType, String pkg, String className) {
            super();
            this.name = name;
            this.dateType = dateType;
            this.pkg = pkg;
            this.className = className;
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Useage:\nDataDescFile\tSourcePath");
        } else {
            genClassFile(args[0], args[1]);
        }
    }
}
