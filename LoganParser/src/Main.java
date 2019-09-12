import java.io.*;
import java.math.BigDecimal;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Hello World!");
        LoganParser parser = new LoganParser("0123456789012345".getBytes(),"0123456789012345".getBytes());
        File srcLogFile = new File("C:\\Users\\xes\\Desktop\\20190828000000_20190829102135");
        FileInputStream inputStream = new FileInputStream(srcLogFile);
        File dstLogfile = new File("C:\\Users\\xes\\Desktop\\20190828000000_20190829102135.log");
        FileOutputStream fos = new FileOutputStream(dstLogfile);

        parser.parse(inputStream,fos);
        
        
//        System.out.println(Math.pow(2,55));
//        
//        BigDecimal bd = new BigDecimal(Math.pow(2,55));  
//        System.out.println(bd.toPlainString());

    }
}
