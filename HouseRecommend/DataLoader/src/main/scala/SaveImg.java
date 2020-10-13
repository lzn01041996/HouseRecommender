import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/13

*/
public class SaveImg {
    public static void main(String[] args) throws IOException {
        File infile = new File("D:\\IntelliJ IDEA-workspace\\HouseRent\\Rent-spiderData\\src\\main\\log\\logs.log");
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String inString = "";
        String path = "D:\\IntelliJ IDEA-workspace\\HouseRent\\HouseRecommend\\DataLoader\\src\\main\\resources\\imgs\\";
        File outfile = new File(path);
        // 声明 HttpURLConnection 请求对象
        HttpURLConnection httpURLConnection = null;

        // 声明 InputStream 输入字节流
        InputStream inputStream = null;

        while ((inString = reader.readLine()) != null){
            System.out.println(inString);
            String[] split = inString.split("\\^");
            String url = split[0];
            String[] split1 = split[1].split("top=");
            String id = split1[1];
            System.out.println(id);
            if (Integer.parseInt(id) <2795){
                continue;
            }else{
                URL uri = new URL(url);
                // 实例化 HttpURLConnection 对象
                httpURLConnection = (HttpURLConnection) uri.openConnection();

                // 设置 HttpURLConnection 请求头部分，通过 chrome 的 F12 查看
                httpURLConnection.setRequestProperty("Cookie", "opqopq=0c01989c7413e5a5d21e68f310c397b5.1573377223; _S=03b161a0b5b7eaaadd261490c514a29d; __guid=16527278.863313966117013800.1573377222710.2178; count=1; tracker=; test_cookie_enable=null");
                httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36");
                System.out.println("开始下载图片...");
                // 从 HttpURLConnection 请求对象中 实例化(获取) 输入字节流
                inputStream = httpURLConnection.getInputStream();

                // 声明 输出流对象, 并实例化为 一个 文件输出流对象; 这里很好理解，实例化文件输出流对象，肯定需要一个文件对象作参数
                OutputStream outputStream = new FileOutputStream(new File(path + id + ".jpg"));

                // 实例化一个 byte[] 对象，用于 io 缓冲区的大小设置
                byte[] bytes = new byte[2048];

                // 接收 每次读取到的 字节数
                int len = 0;

                // 采用 while 循环，只要满足条件，一直循环执行
                while ((len = inputStream.read(bytes)) != -1) { // 从 输入字节流 中读取内容
                    // 将 读取到的 字节 写入文件即可
                    outputStream.write(bytes, 0, len);
                }
            }

        }
        inputStream.close();
        reader.close();
    }
}
