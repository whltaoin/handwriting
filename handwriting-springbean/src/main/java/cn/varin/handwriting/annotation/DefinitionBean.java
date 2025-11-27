package cn.varin.handwriting.annotation;

/**
 * 用于定义bean
 */
public class DefinitionBean {

    private Class clazz;
    private String classType;
    private String className;
    public DefinitionBean(Class clazz, String classType, String className) {
        this.clazz = clazz;
        this.classType = classType;

        this.className = className;
    }
    public DefinitionBean(Class clazz, String className) {
        this.clazz = clazz;

        this.className = className;
    }
    public DefinitionBean() {}

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return "DefinitionBean{" +
                "clazz=" + clazz +
                ", classType='" + classType + '\'' +
                ", className='" + className + '\'' +
                '}';
    }
}
