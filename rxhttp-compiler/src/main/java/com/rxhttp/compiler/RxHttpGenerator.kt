package com.rxhttp.compiler

import com.squareup.javapoet.*
import java.io.File
import java.io.IOException
import java.lang.Deprecated
import java.util.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement
import kotlin.Any
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Int
import kotlin.String
import kotlin.Throws

class RxHttpGenerator {
    private var mParamsAnnotatedClass: ParamsAnnotatedClass? = null
    private var mParserAnnotatedClass: ParserAnnotatedClass? = null
    private var mDomainAnnotatedClass: DomainAnnotatedClass? = null
    private var mConverterAnnotatedClass: ConverterAnnotatedClass? = null
    private var mOkClientAnnotatedClass: OkClientAnnotatedClass? = null
    private var defaultDomain: VariableElement? = null
    fun setAnnotatedClass(annotatedClass: ParamsAnnotatedClass?) {
        mParamsAnnotatedClass = annotatedClass
    }

    fun setAnnotatedClass(annotatedClass: ConverterAnnotatedClass?) {
        mConverterAnnotatedClass = annotatedClass
    }

    fun setAnnotatedClass(annotatedClass: OkClientAnnotatedClass?) {
        mOkClientAnnotatedClass = annotatedClass
    }

    fun setAnnotatedClass(annotatedClass: DomainAnnotatedClass?) {
        mDomainAnnotatedClass = annotatedClass
    }

    fun setAnnotatedClass(annotatedClass: ParserAnnotatedClass?) {
        mParserAnnotatedClass = annotatedClass
    }

    fun setAnnotatedClass(defaultDomain: VariableElement?) {
        this.defaultDomain = defaultDomain
    }


    @Throws(IOException::class)
    fun generateCode(filer: Filer, isAndroidPlatform: Boolean) {
        val httpSenderName = ClassName.get("rxhttp", "HttpSender")
        val logUtilName = ClassName.get("rxhttp.wrapper.utils", "LogUtil")
        val rxHttpPluginsName = ClassName.get("rxhttp", "RxHttpPlugins")
        val converterName = ClassName.get("rxhttp.wrapper.callback", "IConverter")
        val functionsName = ClassName.get("rxhttp.wrapper.callback", "Function")
        val jsonObjectName = ClassName.get("com.google.gson", "JsonObject")
        val jsonArrayName = ClassName.get("com.google.gson", "JsonArray")
        val stringName = ClassName.get(String::class.java)
        val objectName = ClassName.get(Any::class.java)
        val timeUnitName = ClassName.get("java.util.concurrent", "TimeUnit")
        val paramTName = ParameterizedTypeName.get(paramName, TypeVariableName.get("?"))
        val mapKVName = ParameterizedTypeName.get(functionsName, paramTName, paramTName)
        val mapStringName = ParameterizedTypeName.get(functionsName, stringName, stringName)
        val subObject = WildcardTypeName.subtypeOf(TypeName.get(Any::class.java))
        val listName = ParameterizedTypeName.get(ClassName.get(MutableList::class.java), subObject)
        val listObjectName = ParameterizedTypeName.get(ClassName.get(MutableList::class.java), objectName)

        val upFileName = ClassName.get("rxhttp.wrapper.entity", "UpFile")
        val subUpFile = WildcardTypeName.subtypeOf(upFileName)
        val listUpFileName = ParameterizedTypeName.get(ClassName.get(MutableList::class.java), subUpFile)
        val subFile = WildcardTypeName.subtypeOf(TypeName.get(File::class.java))
        val listFileName = ParameterizedTypeName.get(ClassName.get(MutableList::class.java), subFile)
        val noBodyParamName = ClassName.get(packageName, "NoBodyParam")
        val rxHttpNoBodyName = ClassName.get(rxHttpPackage, "RxHttpNoBodyParam")
        val formParamName = ClassName.get(packageName, "FormParam")
        val rxHttpFormName = ClassName.get(rxHttpPackage, "RxHttpFormParam")
        val jsonParamName = ClassName.get(packageName, "JsonParam")
        val rxHttpJsonName = ClassName.get(rxHttpPackage, "RxHttpJsonParam")
        val jsonArrayParamName = ClassName.get(packageName, "JsonArrayParam")
        val rxHttpJsonArrayName = ClassName.get(rxHttpPackage, "RxHttpJsonArrayParam")
        val rxHttpNoBody = ParameterizedTypeName.get(RXHTTP, noBodyParamName, rxHttpNoBodyName)
        val rxHttpForm = ParameterizedTypeName.get(RXHTTP_BODY_PARAM, formParamName, rxHttpFormName)
        val rxHttpJson = ParameterizedTypeName.get(RXHTTP_BODY_PARAM, jsonParamName, rxHttpJsonName)
        val rxHttpJsonArray = ParameterizedTypeName.get(RXHTTP_BODY_PARAM, jsonArrayParamName, rxHttpJsonArrayName)

        val nullableName = ClassName.get("rxhttp.wrapper.annotations", "Nullable")
        val okHttpClientName = ClassName.get("okhttp3", "OkHttpClient")
        val partName = ClassName.get("okhttp3.MultipartBody", "Part")
        val mediaTypeName = ClassName.get("okhttp3", "MediaType")
        val requestBodyName = ClassName.get("okhttp3", "RequestBody")
        val headersName = ClassName.get("okhttp3", "Headers")
        val requestName = ClassName.get("okhttp3", "Request")
        val cacheInterceptorName = ClassName.get("rxhttp.wrapper.intercept", "CacheInterceptor")

        val methodList = ArrayList<MethodSpec>() //方法集合

        methodList.add(
            MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(p, "param")
                .addStatement("this.param = param")
                .build()) //添加构造方法

        methodList.add(
            MethodSpec.methodBuilder("setDebug")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Boolean::class.javaPrimitiveType, "debug")
                .addStatement("setDebug(debug, false)")
                .returns(Void.TYPE)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setDebug")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Boolean::class.javaPrimitiveType, "debug")
                .addParameter(Boolean::class.javaPrimitiveType, "segmentPrint")
                .addStatement("\$T.setDebug(debug, segmentPrint)", logUtilName)
                .returns(Void.TYPE)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(okHttpClientName, "okHttpClient")
                .addStatement("\$T.init(okHttpClient)", httpSenderName)
                .returns(Void.TYPE)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(okHttpClientName, "okHttpClient")
                .addParameter(Boolean::class.javaPrimitiveType, "debug")
                .addStatement("\$T.init(okHttpClient,debug)", httpSenderName)
                .returns(Void.TYPE)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("isInit")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("return \$T.isInit()", httpSenderName)
                .returns(Boolean::class.java)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setResultDecoder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addJavadoc("""
                    设置统一数据解码/解密器，每次请求成功后会回调该接口并传入Http请求的结果
                    通过该接口，可以统一对数据解密，并将解密后的数据返回即可
                    若部分接口不需要回调该接口，发请求前，调用{@link #setDecoderEnabled(boolean)}方法设置false即可
                """.trimIndent())
                .addParameter(mapStringName, "decoder")
                .addStatement("\$T.setResultDecoder(decoder)", rxHttpPluginsName)
                .returns(Void.TYPE)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setConverter")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addJavadoc("设置默认的转换器\n")
                .addParameter(converterName, "converter")
                .addStatement("\$T.setConverter(converter)", rxHttpPluginsName)
                .returns(Void.TYPE)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setOnParamAssembly")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addJavadoc("""
                    设置统一公共参数回调接口,通过该接口,可添加公共参数/请求头，每次请求前会回调该接口
                    若部分接口不需要添加公共参数,发请求前，调用{@link #setAssemblyEnabled(boolean)}方法设置false即可
                """.trimIndent())
                .addParameter(mapKVName, "onParamAssembly")
                .addStatement("\$T.setOnParamAssembly(onParamAssembly)", rxHttpPluginsName)
                .returns(Void.TYPE)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("connectTimeout")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Int::class.javaPrimitiveType, "connectTimeout")
                .addStatement("connectTimeoutMillis = connectTimeout")
                .addStatement("return (R)this")
                .returns(r)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("readTimeout")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Int::class.javaPrimitiveType, "readTimeout")
                .addStatement("readTimeoutMillis = readTimeout")
                .addStatement("return (R)this")
                .returns(r)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("writeTimeout")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Int::class.javaPrimitiveType, "writeTimeout")
                .addStatement("writeTimeoutMillis = writeTimeout")
                .addStatement("return (R)this")
                .returns(r)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("getOkHttpClient")
                .addModifiers(Modifier.PUBLIC)
                .addCode("""
                    if (realOkClient != null) return realOkClient;
                    final OkHttpClient okHttpClient = okClient;
                    OkHttpClient.Builder builder = null;
                    
                    if (connectTimeoutMillis != 0) {
                      if (builder == null) builder = okHttpClient.newBuilder();
                      builder.connectTimeout(connectTimeoutMillis, ${'$'}T.MILLISECONDS);
                    }
                    
                    if (readTimeoutMillis != 0) {
                      if (builder == null) builder = okHttpClient.newBuilder();
                      builder.readTimeout(readTimeoutMillis, ${'$'}T.MILLISECONDS);
                    }

                    if (writeTimeoutMillis != 0) {
                      if (builder == null) builder = okHttpClient.newBuilder();
                      builder.writeTimeout(writeTimeoutMillis, ${'$'}T.MILLISECONDS);
                    }
                    
                    if (param.getCacheMode() != CacheMode.ONLY_NETWORK) {                      
                      if (builder == null) builder = okHttpClient.newBuilder();              
                      builder.addInterceptor(new ${'$'}T(param.getCacheStrategy()));
                    }
                                                                                            
                    realOkClient = builder != null ? builder.build() : okHttpClient;
                    return realOkClient;
                """.trimIndent(), timeUnitName, timeUnitName, timeUnitName, cacheInterceptorName)
                .returns(okHttpClientName).build())

        if (isDependenceRxJava()) {
            val disposableName = getClassName("Disposable")
            methodList.add(
                MethodSpec.methodBuilder("dispose")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(disposableName, "disposable")
                    .addStatement("if (!isDisposed(disposable)) disposable.dispose()")
                    .build())

            methodList.add(
                MethodSpec.methodBuilder("isDisposed")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(disposableName, "disposable")
                    .addStatement("return disposable == null || disposable.isDisposed()")
                    .returns(Boolean::class.java).build())
        }

        methodList.add(
            MethodSpec.methodBuilder("getParam")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return param")
                .returns(p)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setParam")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(p, "param")
                .addStatement("this.param = param")
                .addStatement("return (R)this")
                .returns(r)
                .build())
        methodList.addAll(mParamsAnnotatedClass!!.getMethodList(filer))
        methodList.addAll(mParserAnnotatedClass!!.getMethodList(filer))
        methodList.addAll(mConverterAnnotatedClass!!.methodList)
        methodList.addAll(mOkClientAnnotatedClass!!.methodList)
        val method = MethodSpec.methodBuilder("addDefaultDomainIfAbsent")
            .addJavadoc("给Param设置默认域名(如何缺席的话)，此方法会在请求发起前，被RxHttp内部调用\n")
            .addModifiers(Modifier.PRIVATE)
            .addParameter(p, "param")
        if (defaultDomain != null) {
            method.addStatement("String newUrl = addDomainIfAbsent(param.getSimpleUrl(), \$T.\$L)",
                ClassName.get(defaultDomain!!.enclosingElement.asType()),
                defaultDomain!!.simpleName.toString())
                .addStatement("param.setUrl(newUrl)")
        }
        method.addStatement("return param")
            .returns(p)
        methodList.add(method.build())
        methodList.addAll(mDomainAnnotatedClass!!.methodList)

        methodList.add(
            MethodSpec.methodBuilder("format")
                .addJavadoc("Returns a formatted string using the specified format string and arguments.")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(String::class.java, "url")
                .addParameter(ArrayTypeName.of(Any::class.java), "formatArgs")
                .varargs()
                .addStatement("return formatArgs == null || formatArgs.length == 0 ? url : String.format(url, formatArgs)")
                .returns(String::class.java)
                .build())
        val converterSpec = FieldSpec.builder(converterName, "converter", Modifier.PROTECTED)
            .initializer("\$T.getConverter()", rxHttpPluginsName)
            .build()

        val okHttpClientSpec = FieldSpec.builder(okHttpClientName, "okClient", Modifier.PRIVATE)
            .initializer("\$T.getOkHttpClient()", httpSenderName)
            .build()
        val build = AnnotationSpec.builder(SuppressWarnings::class.java)
            .addMember("value", "\"unchecked\"")
            .build()
        val baseRxHttpName = ClassName.get(rxHttpPackage, "BaseRxHttp")

        val isAsyncField = FieldSpec
            .builder(Boolean::class.javaPrimitiveType, "isAsync", Modifier.PROTECTED)
            .initializer("true")
            .build()

        val rxHttpBuilder = TypeSpec.classBuilder(RXHttp_CLASS_NAME)
            .addJavadoc("""
                Github
                https://github.com/liujingxing/RxHttp
                https://github.com/liujingxing/RxLife
                https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
                https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
            """.trimIndent())
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(build)
            .addField(p, "param", Modifier.PROTECTED)
            .addField(Int::class.javaPrimitiveType, "connectTimeoutMillis", Modifier.PRIVATE)
            .addField(Int::class.javaPrimitiveType, "readTimeoutMillis", Modifier.PRIVATE)
            .addField(Int::class.javaPrimitiveType, "writeTimeoutMillis", Modifier.PRIVATE)
            .addField(okHttpClientName, "realOkClient", Modifier.PRIVATE)
            .addField(okHttpClientSpec)
            .addField(isAsyncField)
            .addField(converterSpec)
            .addField(requestName, "request", Modifier.PUBLIC)
            .superclass(baseRxHttpName)
            .addTypeVariable(p)
            .addTypeVariable(r)
            .addMethods(methodList)

        // Write file
        JavaFile.builder(rxHttpPackage, rxHttpBuilder.build())
            .build().writeTo(filer)

        methodList.clear()
        methodList.add(
            MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(noBodyParamName, "param")
                .addStatement("super(param)")
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addEncoded")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(Any::class.java, "value")
                .addStatement("param.addEncoded(key,value)")
                .addStatement("return this")
                .returns(rxHttpNoBodyName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("removeAllBody")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("param.removeAllBody()")
                .addStatement("return this")
                .returns(rxHttpNoBodyName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("removeAllBody")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addStatement("param.removeAllBody(key)")
                .addStatement("return this")
                .returns(rxHttpNoBodyName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("set")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(Any::class.java, "value")
                .addStatement("param.set(key,value)")
                .addStatement("return this")
                .returns(rxHttpNoBodyName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setEncoded")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(Any::class.java, "value")
                .addStatement("param.setEncoded(key,value)")
                .addStatement("return this")
                .returns(rxHttpNoBodyName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("queryValue")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addStatement("return param.queryValue(key)")
                .returns(Any::class.java)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("queryValues")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addStatement("return param.queryValues(key)")
                .returns(listObjectName)
                .build())
        val rxHttpNoBodySpec = TypeSpec.classBuilder("RxHttpNoBodyParam")
            .addJavadoc("""
                Github
                https://github.com/liujingxing/RxHttp
                https://github.com/liujingxing/RxLife
                https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
                https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
            """.trimIndent())
            .addModifiers(Modifier.PUBLIC)
            .superclass(rxHttpNoBody)
            .addMethods(methodList)
            .build()
        JavaFile.builder(rxHttpPackage, rxHttpNoBodySpec)
            .build().writeTo(filer)

        methodList.clear()
        methodList.add(
            MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(formParamName, "param")
                .addStatement("super(param)")
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addEncoded")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(Any::class.java, "value")
                .addStatement("param.addEncoded(key,value)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("removeAllBody")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("param.removeAllBody()")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("removeAllBody")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addStatement("param.removeAllBody(key)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("set")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(Any::class.java, "value")
                .addStatement("param.set(key,value)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setEncoded")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(Any::class.java, "value")
                .addStatement("param.setEncoded(key,value)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("queryValue")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addStatement("return param.queryValue(key)")
                .returns(Any::class.java)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("queryValues")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addStatement("return param.queryValues(key)")
                .returns(listObjectName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("add")
                .addAnnotation(Deprecated::class.java)
                .addJavadoc("@deprecated please user {@link #addFile(String,File)} instead\n")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(File::class.java, "file")
                .addStatement("param.addFile(key,file)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(File::class.java, "file")
                .addStatement("param.addFile(key,file)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(String::class.java, "filePath")
                .addStatement("param.addFile(key,filePath)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(String::class.java, "filename")
                .addParameter(String::class.java, "filePath")
                .addStatement("param.addFile(key, filename, filePath)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(String::class.java, "filename")
                .addParameter(File::class.java, "file")
                .addStatement("param.addFile(key, filename, file)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(upFileName, "file")
                .addStatement("param.addFile(file)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(listFileName, "fileList")
                .addStatement("param.addFile(key,fileList)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(listUpFileName, "fileList")
                .addStatement("param.addFile(fileList)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())


        val mediaTypeParam = ParameterSpec.builder(mediaTypeName, "contentType")
            .addAnnotation(nullableName)
            .build()

        methodList.add(
            MethodSpec.methodBuilder("addPart")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mediaTypeParam)
                .addParameter(ByteArray::class.java, "content")
                .addStatement("param.addPart(contentType, content)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addPart")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mediaTypeParam)
                .addParameter(ByteArray::class.java, "content")
                .addParameter(Int::class.java, "offset")
                .addParameter(Int::class.java, "byteCount")
                .addStatement("param.addPart(contentType, content, offset, byteCount)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        if (isAndroidPlatform) {

            val contextName = ClassName.get("android.content", "Context")
            val uriName = ClassName.get("android.net", "Uri")

            val listUriName = ParameterizedTypeName.get(ClassName.get(MutableList::class.java), uriName)
            val mapUriName = ParameterizedTypeName.get(ClassName.get(MutableMap::class.java), stringName, uriName)

            val kotlinExtensionsName = ClassName.get("rxhttp.wrapper.utils", "UriUtil")
            val mapEntryName = ClassName.get("java.util.Map", "Entry")

            methodList.add(
                MethodSpec.methodBuilder("addPart")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(contextName, "context")
                    .addParameter(uriName, "uri")
                    .addStatement("param.addPart(\$T.asRequestBody(uri, context))", kotlinExtensionsName)
                    .addStatement("return this")
                    .returns(rxHttpFormName)
                    .build())

            methodList.add(
                MethodSpec.methodBuilder("addPart")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(contextName, "context")
                    .addParameter(String::class.java, "key")
                    .addParameter(uriName, "uri")
                    .addStatement("param.addPart(\$T.asPart(uri, context, key))", kotlinExtensionsName)
                    .addStatement("return this")
                    .returns(rxHttpFormName)
                    .build())

            methodList.add(
                MethodSpec.methodBuilder("addPart")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(contextName, "context")
                    .addParameter(String::class.java, "key")
                    .addParameter(String::class.java, "fileName")
                    .addParameter(uriName, "uri")
                    .addStatement("param.addPart(\$T.asPart(uri, context, key, fileName))", kotlinExtensionsName)
                    .addStatement("return this")
                    .returns(rxHttpFormName)
                    .build())

            methodList.add(
                MethodSpec.methodBuilder("addPart")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(contextName, "context")
                    .addParameter(uriName, "uri")
                    .addParameter(mediaTypeParam)
                    .addStatement("param.addPart(\$T.asRequestBody(uri, context, contentType))", kotlinExtensionsName)
                    .addStatement("return this")
                    .returns(rxHttpFormName)
                    .build())

            methodList.add(
                MethodSpec.methodBuilder("addPart")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(contextName, "context")
                    .addParameter(String::class.java, "key")
                    .addParameter(uriName, "uri")
                    .addParameter(mediaTypeParam)
                    .addStatement("param.addPart(\$T.asPart(uri, context, key, null, contentType))", kotlinExtensionsName)
                    .addStatement("return this")
                    .returns(rxHttpFormName)
                    .build())

            methodList.add(
                MethodSpec.methodBuilder("addPart")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(contextName, "context")
                    .addParameter(String::class.java, "key")
                    .addParameter(String::class.java, "filename")
                    .addParameter(uriName, "uri")
                    .addParameter(mediaTypeParam)
                    .addStatement("param.addPart(\$T.asPart(uri, context, key, filename, contentType))", kotlinExtensionsName)
                    .addStatement("return this")
                    .returns(rxHttpFormName)
                    .build())

            methodList.add(
                MethodSpec.methodBuilder("addParts")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(contextName, "context")
                    .addParameter(mapUriName, "uriMap")
                    .addCode("""
                        for (${"$"}T<String, Uri> entry : uriMap.entrySet()) {
                            addPart(context, entry.getKey(), entry.getValue());       
                        }
                        return this;
                    """.trimIndent(),mapEntryName)
                    .returns(rxHttpFormName)
                    .build())

            methodList.add(
                MethodSpec.methodBuilder("addParts")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(contextName, "context")
                    .addParameter(listUriName, "uris")
                    .addCode("""
                        for (Uri uri : uris) {    
                            addPart(context, uri);
                        }
                        return this;                         
                    """.trimIndent())
                    .returns(rxHttpFormName)
                    .build())

            methodList.add(
                MethodSpec.methodBuilder("addParts")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(contextName, "context")
                    .addParameter(String::class.java, "key")
                    .addParameter(listUriName, "uris")
                    .addCode("""
                        for (Uri uri : uris) {          
                            addPart(context, key, uri);
                        }
                        return this;                               
                    """.trimIndent())
                    .returns(rxHttpFormName)
                    .build())

            methodList.add(
                MethodSpec.methodBuilder("addParts")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(contextName, "context")
                    .addParameter(listUriName, "uris")
                    .addParameter(mediaTypeParam)
                    .addCode("""
                        for (Uri uri : uris) {                 
                            addPart(context, uri, contentType);
                        }
                        return this;                                      
                    """.trimIndent())
                    .returns(rxHttpFormName)
                    .build())

            methodList.add(
                MethodSpec.methodBuilder("addParts")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(contextName, "context")
                    .addParameter(String::class.java, "key")
                    .addParameter(listUriName, "uris")
                    .addParameter(mediaTypeParam)
                    .addCode("""
                        for (Uri uri : uris) {                       
                            addPart(context, key, uri, contentType);
                        }
                        return this;                                            
                    """.trimIndent())
                    .returns(rxHttpFormName)
                    .build())
        }

        methodList.add(
            MethodSpec.methodBuilder("addPart")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(partName, "part")
                .addStatement("param.addPart(part)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addPart")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(requestBodyName, "requestBody")
                .addStatement("param.addPart(requestBody)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addPart")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(headersName, "headers")
                .addParameter(requestBodyName, "requestBody")
                .addStatement("param.addPart(headers, requestBody)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addFormDataPart")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(stringName, "key")
                .addParameter(stringName, "fileName")
                .addParameter(requestBodyName, "requestBody")
                .addStatement("param.addFormDataPart(key, fileName, requestBody)")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setMultiForm")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("param.setMultiForm()")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build())

        val rxHttpFormSpec = TypeSpec.classBuilder("RxHttpFormParam")
            .addJavadoc("""
                Github
                https://github.com/liujingxing/RxHttp
                https://github.com/liujingxing/RxLife
                https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
                https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
            """.trimIndent())
            .addModifiers(Modifier.PUBLIC)
            .superclass(rxHttpForm)
            .addMethods(methodList)

        JavaFile.builder(rxHttpPackage, rxHttpFormSpec.build())
            .build().writeTo(filer)

        methodList.clear()

        methodList.add(
            MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(jsonParamName, "param")
                .addStatement("super(param)")
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addAll")
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("""
                    将Json对象里面的key-value逐一取出，添加到另一个Json对象中，
                    输入非Json对象将抛出{@link IllegalStateException}异常
                """.trimIndent())
                .addParameter(String::class.java, "jsonObject")
                .addStatement("param.addAll(jsonObject)")
                .addStatement("return this")
                .returns(rxHttpJsonName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addAll")
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("将Json对象里面的key-value逐一取出，添加到另一个Json对象中\n")
                .addParameter(jsonObjectName, "jsonObject")
                .addStatement("param.addAll(jsonObject)")
                .addStatement("return this")
                .returns(rxHttpJsonName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addJsonElement")
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("添加一个JsonElement对象(Json对象、json数组等)\n")
                .addParameter(String::class.java, "key")
                .addParameter(String::class.java, "jsonElement")
                .addStatement("param.addJsonElement(key,jsonElement)")
                .addStatement("return this")
                .returns(rxHttpJsonName)
                .build())
        val rxHttpJsonSpec = TypeSpec.classBuilder("RxHttpJsonParam")
            .addJavadoc("""
                Github
                https://github.com/liujingxing/RxHttp
                https://github.com/liujingxing/RxLife
                https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
                https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
            """.trimIndent())
            .addModifiers(Modifier.PUBLIC)
            .superclass(rxHttpJson)
            .addMethods(methodList)
            .build()
        JavaFile.builder(rxHttpPackage, rxHttpJsonSpec)
            .build().writeTo(filer)

        methodList.clear()

        methodList.add(
            MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(jsonArrayParamName, "param")
                .addStatement("super(param)")
                .build())

        methodList.add(
            MethodSpec.methodBuilder("add")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Any::class.java, "object")
                .addStatement("param.add(object)")
                .addStatement("return this")
                .returns(rxHttpJsonArrayName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addAll")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(listName, "list")
                .addStatement("param.addAll(list)")
                .addStatement("return this")
                .returns(rxHttpJsonArrayName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addAll")
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("添加多个对象，将字符串转JsonElement对象,并根据不同类型,执行不同操作,可输入任意非空字符串\n")
                .addParameter(String::class.java, "jsonElement")
                .addStatement("param.addAll(jsonElement)")
                .addStatement("return this")
                .returns(rxHttpJsonArrayName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addAll")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(jsonArrayName, "jsonArray")
                .addStatement("param.addAll(jsonArray)")
                .addStatement("return this")
                .returns(rxHttpJsonArrayName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addAll")
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("将Json对象里面的key-value逐一取出，添加到Json数组中，成为单独的对象\n")
                .addParameter(jsonObjectName, "jsonObject")
                .addStatement("param.addAll(jsonObject)")
                .addStatement("return this")
                .returns(rxHttpJsonArrayName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addJsonElement")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "jsonElement")
                .addStatement("param.addJsonElement(jsonElement)")
                .addStatement("return this")
                .returns(rxHttpJsonArrayName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addJsonElement")
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("添加一个JsonElement对象(Json对象、json数组等)\n")
                .addParameter(String::class.java, "key")
                .addParameter(String::class.java, "jsonElement")
                .addStatement("param.addJsonElement(key,jsonElement)")
                .addStatement("return this")
                .returns(rxHttpJsonArrayName)
                .build())

        val rxHttpJsonArraySpec = TypeSpec.classBuilder("RxHttpJsonArrayParam")
            .addJavadoc("""
                Github
                https://github.com/liujingxing/RxHttp
                https://github.com/liujingxing/RxLife
                https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
                https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
            """.trimIndent())
            .addModifiers(Modifier.PUBLIC)
            .superclass(rxHttpJsonArray)
            .addMethods(methodList)
            .build()
        JavaFile.builder(rxHttpPackage, rxHttpJsonArraySpec)
            .build().writeTo(filer)
    }
}

const val RXHttp_CLASS_NAME = "RxHttp"
const val packageName = "rxhttp.wrapper.param"
var RXHTTP = ClassName.get(rxHttpPackage, RXHttp_CLASS_NAME)
var RXHTTP_BODY_PARAM = ClassName.get(rxHttpPackage, "RxHttpBodyParam")
private val paramName = ClassName.get(packageName, "Param")
var p = TypeVariableName.get("P", paramName)  //泛型P
var r = TypeVariableName.get("R", RXHTTP)     //泛型R