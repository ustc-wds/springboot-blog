package com.hyf.controller;

import com.hyf.constant.CodeType;
import com.hyf.service.ArticleService;
import com.hyf.service.TagService;
import com.hyf.utils.DataMap;
import com.hyf.utils.JsonResult;
import com.hyf.utils.StringUtil;
import com.hyf.utils.TransCodingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: zhangocean
 * @Date: 2018/7/16 21:27
 * Describe:
 */
@Controller
public class TagsControl {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    TagService tagService;
    @Autowired
    ArticleService articleService;

    /**
     * 分页获得该标签下的文章
     * @param tag
     * @return
     */
    @PostMapping(value = "/getTagArticle", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String getTagArticle(@RequestParam("tag") String tag,
                                @RequestParam("rows") int rows,
                                @RequestParam("pageNum") int pageNum){
        try {
            if(tag.equals(StringUtil.BLANK)){
                return JsonResult.build(tagService.findTagsCloud()).toJSON();
            }

            tag = TransCodingUtil.unicodeToString(tag);
            DataMap data = articleService.findArticleByTag(tag, rows, pageNum);
            return JsonResult.build(data).toJSON();
        } catch (Exception e){
            log.error("Get tags exception", e);
        }
        return JsonResult.fail(CodeType.SERVER_EXCEPTION).toJSON();
    }

    /**
     * 跳转到标签页
     */
    @GetMapping("/tags")
    public String tags(HttpServletResponse response,
                       HttpServletRequest request){
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        request.getSession().removeAttribute("lastUrl");
        String tag = request.getParameter("tag");

        if(tag != null && !tag.equals(StringUtil.BLANK)){
            response.setHeader("tag", TransCodingUtil.stringToUnicode(tag));
        }

        return "tags";
    }
}
