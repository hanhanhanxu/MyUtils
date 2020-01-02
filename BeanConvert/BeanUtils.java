package BeanUtils;

import BeanUtils.JavaBean.People;
import BeanUtils.JavaBean.Work;
import BeanUtils.JavaBean.WorkBean;
import BeanUtils.JavaBean.WorkInfo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: HanXu
 * on 2019/12/31
 * Class description: Bean转换, 组合Bean转换为各个自Bean，转换依据是属性名称相同。
 */
public class BeanUtils {

    /**
     * 拷贝Bean：传入源对象，和一些目标类. 若源对象中存在和目标类中名字相同的属性，则new出对象并将值赋予，返回转换后的Bean的类和对象Map集合.
     * @param srcObj 源对象
     * @param tarCls 目标类
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchFieldException
     */
    public static Map beanConvert(Object srcObj, Class... tarCls) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        //参数校验
        if (srcObj==null || tarCls==null || tarCls.length==0) {
            return null;
        }
        //获取源类、属性信息
        Class<?> srcCls = srcObj.getClass();
        Field[] srcFields = srcCls.getDeclaredFields();
        //获取每个目标类的所有属性名字
        Map<Integer, Set> tarFieldsMap = new HashMap();
        for (int i = 0; i < tarCls.length; i++) {
            Class tarC = tarCls[i];
            Field[] tarFields = tarC.getDeclaredFields();
            Set tarFieldsSet = new HashSet();
            for (Field field : tarFields) {
                tarFieldsSet.add(field.getName());
            }
            tarFieldsMap.put(i, tarFieldsSet);
        }

        //遍历源对象的所有属性，再遍历目标类，判断是否某个目标类中存在这个属性，是，就将其set.  时间复杂度：fields的个数*n个类≈O(n)
        Map<Class, Object> tarClsObjMap = new HashMap();//维护目标类和对象的映射关系
        for (Field srcField : srcFields) {
            for (int i = 0; i < tarCls.length; i++) {
                Set tarFieldsSet = tarFieldsMap.get(i);
                if (tarFieldsSet.contains(srcField.getName())) {
                    //拿到对应目标类和对象
                    Class tarC = tarCls[i];
                    if (!tarClsObjMap.containsKey(tarC)) {
                        Object tarOTemp = tarC.newInstance();
                        tarClsObjMap.put(tarC,tarOTemp);
                    }
                    Object tarO = tarClsObjMap.get(tarC);
                    //执行srcObj的此字段的get方法拿到数据
                    srcField.setAccessible(true);
                    Object fieldValue = srcField.get(srcObj);
                    //执行tarO的此字段的set方法填入数据
                    Field tarField = tarC.getDeclaredField(srcField.getName());
                    tarField.setAccessible(true);
                    tarField.set(tarO,fieldValue);
                }
            }
        }
        return tarClsObjMap;
    }


    /**
     * setter 、 getter方法省略
     *
     * public class WorkBean {
     *     private String wtitle;//必须
     *
     *     private String wintro;//可选
     *
     *     private String wimg;//可选
     *
     *     private String winfocontent;//长文本
     * }
     *
     * public class Work {
     *     private Integer wid;//自增
     *
     *     private String wtitle;//必须
     *
     *     private String wintro;//可选
     *
     *     private String wimg;//可选
     *
     *     private Date createTime;//数据库插入
     *
     *     private Date updateTime;//数据库插入
     * }
     *
     * public class Workinfo {
     *     private String winfoid;//UUID
     *
     *     private Integer wid;//外键
     *
     *     private String winfocontent;//长文本
     * }
     */
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        WorkBean workBean = new WorkBean();
        workBean.setWtitle("title");
        workBean.setWintro("intro");
        workBean.setWimg("img");
        workBean.setWinfocontent("content");

        //取部分
        System.out.println("****************取部分****************");
        Map m = beanConvert(workBean, Work.class);
        if (m!=null) {
            Work work = (Work) m.get(Work.class);

            System.out.println(work);
        }
        System.out.println();

        //取全部
        System.out.println("****************取全部****************");
        Map map = beanConvert(workBean, Work.class, WorkInfo.class);
        if (map!=null) {
            Work work = (Work) map.get(Work.class);
            WorkInfo workInfo = (WorkInfo) map.get(WorkInfo.class);

            System.out.println(work);
            System.out.println(workInfo);
        }
        System.out.println();

        //不确定是否存在的元素
        System.out.println("****************不确定是否存在的元素****************");
        Map map1 = beanConvert(workBean, Work.class, People.class);
        if (map1!=null) {
            Work work = (Work) map1.get(Work.class);
            People people = (People) map1.get(People.class);

            System.out.println(work);
            System.out.println(people);
        }
        System.out.println();

        System.out.println("****************org.springframework.beans.BeanUtils.copyProperties（src, target）****************");
        Work work = new Work();
        org.springframework.beans.BeanUtils.copyProperties(workBean,work);
        System.out.println(work);
    }
}
