package xyz.kail.sharing.component.demo.spi;

import java.sql.Driver;
import java.util.Iterator;
import java.util.ServiceLoader;

public class SpiMain {

    public static void main(String[] args) {
// 加载 java.sql.Driver 的实现类
ServiceLoader<Driver> sl = ServiceLoader.load(java.sql.Driver.class);
Iterator<Driver> iterator = sl.iterator();
while (iterator.hasNext()) {
    // Class.forName("com.mysql.jdbc.Driver")
    Driver driver = iterator.next();
    System.out.println(driver.getClass().getName());
}
    }

}
