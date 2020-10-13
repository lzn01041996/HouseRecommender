import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/13

*/
public class testImg {

    public static void main(String[] args) throws IOException {
        File infile = new File("D:\\IntelliJ IDEA-workspace\\HouseRent\\HouseRecommend\\DataLoader\\src\\main\\resources\\logs.csv");
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String inString = "";
        String path = "D:\\IntelliJ IDEA-workspace\\HouseRent\\HouseRecommend\\DataLoader\\src\\main\\resources\\hid.csv";
        // 声明 InputStream 输入字节流

        Set<String> set = new TreeSet();
        StringBuffer buffer = new StringBuffer();
        while ((inString = reader.readLine()) != null){
            String[] split = inString.split("\\^");
            String url = split[1].split("top=")[1]+"," + split[2].split(" ")[0]+"\n";
            set.add(url);
        }
        System.out.println(set.size());
        for (String s : set) {
            buffer.append(s);
        }
        //构建FileOutputStream对象,文件不存在会自动新建
        FileOutputStream fop = new FileOutputStream(path);
        // 构建OutputStreamWriter对象,参数可以指定编码"UTF-8";不设置，默认为操作系统默认编码;
        OutputStreamWriter writer = new OutputStreamWriter(fop, "UTF-8");
        //写入缓冲区
        writer.append(buffer);
        // 关闭写入流,同时会把缓冲区内容写入文件
        writer.close();
        //关闭输出流，释放系统资源
        fop.close();
        reader.close();
    }
}
