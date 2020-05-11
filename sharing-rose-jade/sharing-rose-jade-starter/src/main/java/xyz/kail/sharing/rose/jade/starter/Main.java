package xyz.kail.sharing.rose.jade.starter;

import net.paoding.rose.jade.statement.expression.ExqlPattern;
import net.paoding.rose.jade.statement.expression.impl.ExqlContextImpl;
import net.paoding.rose.jade.statement.expression.impl.ExqlPatternImpl;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {
        // 转换语句中的表达式
        String sql = "insert ignore into table_name (`id`,`uid`,`favable_id`,`addtime`,`ranking`) values (:1,:2,now(),0)";
        ExqlPattern pattern = ExqlPatternImpl.compile(sql);
        ExqlContextImpl context = new ExqlContextImpl(sql.length() + 32);

        Map<String, Object> parametersAsMap = new HashMap<String, Object>();
        parametersAsMap.put(":1", "p1");
        parametersAsMap.put(":2", "p2");

        String result = pattern.execute(context, parametersAsMap);
        System.out.println(result);
    }


}
