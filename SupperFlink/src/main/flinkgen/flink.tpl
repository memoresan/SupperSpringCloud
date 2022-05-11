#set($para="")
#set($value="")
#set($temp="")
#foreach(${next} in ${input})
    #set($temp="a${velocityCount}")
    #if(${velocityCount}==1)
        #set($para="${next} $temp")
        #set($value="$temp")
    #else
        #set($para="$para,${next} $temp")
        #set($value="$value,$temp")
    #end
#end

@UdfName("${udfname}")
public static class ${name} extends ScalarFunction
{
    public ${output} eval(${para})
    {
        return ${method}(${value});
    }
}