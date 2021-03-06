package com.vless.data.content.controller

import com.vless.data.common.BizException
import com.vless.data.content.model.News
import com.vless.data.content.model.Product
import com.vless.data.content.query.NewsQuery
import com.vless.data.content.query.ProductIndexQuery
import com.vless.data.content.query.ProductQuery
import com.vless.data.content.result.PageResult
import com.vless.data.content.result.ProductIndexResult
import com.vless.data.content.result.ProductResult
import com.vless.data.content.service.NewsServiceAware
import com.vless.data.content.service.ProductServiceAware
import com.vless.data.content.service.ProductTypeServiceAware
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.util.*
import javax.validation.Valid
import kotlin.collections.ArrayList

@RestController
@RequestMapping("/api/product")
@Api("产品管理")
class ProductController {

    @Autowired
    lateinit var productService: ProductServiceAware

    @Autowired
    lateinit var productTypeService: ProductTypeServiceAware

    @ApiIgnore
    @PostMapping("/save")
    fun save(@RequestBody @Valid product: Product, result:BindingResult):Product{
        if(product.logo!!.startsWith("http"))
            product.logo=product.logo!!.substring(product.logo!!.lastIndexOf("/")+1,product.logo!!.length)
        product.gmtCreate=Date()
        return if(result.hasErrors())
            throw BizException("提交信息有误")
        else
            productService.save(product)
    }

    @ApiIgnore
    @PostMapping("/update")
    fun update(@RequestBody @Valid product:Product, result: BindingResult):Product{
        if(product.logo!!.startsWith("http"))
            product.logo=product.logo!!.substring(product.logo!!.lastIndexOf("/")+1,product.logo!!.length)
        return if(result.hasErrors())
            throw BizException("提交信息有误")
        else
            productService.save(product)
    }

    @ApiIgnore
    @RequestMapping("/remove/{id}",method = arrayOf(RequestMethod.DELETE))
    fun delete(@PathVariable id:Long) = productService.delete(id)

    @ApiOperation("产品列表")
    @ApiImplicitParams(
            ApiImplicitParam(name = "title", value = "标题", dataType = "String", required = false, paramType = "query" ),
            ApiImplicitParam(name = "productTypeParentId", value = "一级产品分类id", dataType = "Long", required = false, paramType = "query" ),
            ApiImplicitParam(name = "page", value = "页码", dataType = "Long", required = false, paramType = "query" ),
            ApiImplicitParam(name = "limit", value = "每页条数", dataType = "Long", required = false, paramType = "query" )
    )
    @GetMapping("/list")
    fun findPage(@ApiIgnore productQuery: ProductQuery):PageResult = productService.findPage(productQuery)

    @ApiOperation("分类产品列表")
    @ApiImplicitParams(
            ApiImplicitParam(name = "title", value = "标题", dataType = "String", required = false, paramType = "query" ),
            ApiImplicitParam(name = "productTypeId", value = "一级产品分类id", dataType = "Long", required = true, paramType = "query" )
    )
    @GetMapping("/result")
    fun findByType(@ApiIgnore productQuery: ProductIndexQuery):ProductIndexResult{
        if(productQuery.productTypeId<=0)
            throw BizException("productTypeParentId不能<=0")
        var result=ProductIndexResult()
        result.productTypeParentId=productQuery.productTypeId
        var list:MutableList<ProductIndexResult.ProductNextCat> = ArrayList()
        val list1=productTypeService.findByParent(productQuery.productTypeId)
        for(pt in list1){
            var cat=ProductIndexResult.ProductNextCat()
            cat.productTypeId=pt.id
            cat.productTypeName=pt.label
            productQuery.productTypeId=pt.id
            val list2=productService.findList(productQuery)
            var listPR:MutableList<ProductResult> = ArrayList()
            for(pt2 in list2){
                listPR.add(pt2)
            }
            cat.list=listPR
            list.add(cat)
        }
        result.list=list
        return result
    }


    @ApiOperation("产品详情")
    @ApiImplicitParam(name = "id", value = "产品id", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/{id}")
    fun findById(@PathVariable id:Long):Product = productService.findById(id)

}