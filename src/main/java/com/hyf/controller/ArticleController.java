package com.hyf.controller;

import com.hyf.aspect.annotation.PermissionCheck;
import com.hyf.bean.ArticleLikesRecord;
import com.hyf.constant.CodeType;
import com.hyf.redis.HashRedisUtil;
import com.hyf.redis.RedisService;
import com.hyf.redis.StringRedisUtil;
import com.hyf.service.ArticleLikesRecordService;
import com.hyf.service.ArticleService;
import com.hyf.service.UserService;
import com.hyf.utils.DataMap;
import com.hyf.utils.JsonResult;
import com.hyf.utils.StringUtil;
import com.hyf.utils.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Map;

@Controller
public class ArticleController {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ArticleService articleService;
    @Autowired
    ArticleLikesRecordService articleLikesRecordService;
    @Autowired
    UserService userService;
    @Autowired
    RedisService redisService;

    /**
     *  获取文章
     * @param articleId 文章id
     */
    @ResponseBody
    @PostMapping(value = "/getArticleByArticleId", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getArticleById(@RequestParam("articleId") String articleId,
                                 @AuthenticationPrincipal Principal principal){
        String username = null;
        try {
            if(principal != null){
                username = principal.getName();
            }
            DataMap data = articleService.getArticleByArticleId(Long.parseLong(articleId),username);
            return JsonResult.build(data).toJSON();
        } catch (Exception e){
            log.error("[{}] get article [{}] exception", username, articleId, e);
        }
        return JsonResult.fail(CodeType.SERVER_EXCEPTION).toJSON();

    }

    /**
     * 点赞
     * @param articleId 文章号
     */
    @GetMapping(value = "/addArticleLike", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PermissionCheck(value = "ROLE_USER")
    public String addArticleLike(@RequestParam("articleId") String articleId,
                                 @AuthenticationPrincipal Principal principal){
        String username = principal.getName();
        try {
            if(articleLikesRecordService.isLiked(Long.parseLong(articleId), username)){
                return JsonResult.fail(CodeType.ARTICLE_HAS_THUMBS_UP).toJSON();
            }

            DataMap data = articleService.updateLikeByArticleId(Long.parseLong(articleId));

            ArticleLikesRecord articleLikesRecord = new ArticleLikesRecord(Long.parseLong(articleId), userService.findIdByUsername(username), new TimeUtil().getFormatDateForFive());
            articleLikesRecordService.insertArticleLikesRecord(articleLikesRecord);
            redisService.readThumbsUpRecordOnRedis(StringUtil.ARTICLE_THUMBS_UP, 1);
            return JsonResult.build(data).toJSON();
        } catch (Exception e){
            log.error("[{}] like article [{}] exception", username, articleId, e);
        }
        return JsonResult.fail(CodeType.SERVER_EXCEPTION).toJSON();

    }
    /**
     * 跳转到文章显示页
     */
    @GetMapping("/article/{articleId}")
    public String show(@PathVariable("articleId") long articleId,
                       HttpServletResponse response,
                       Model model,
                       HttpServletRequest request){
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        request.getSession().removeAttribute("lastUrl");

        Map<String, String> articleMap = articleService.findArticleTitleByArticleId(articleId);
        if(articleMap.get("articleTitle") != null){
            model.addAttribute("articleTitle",articleMap.get("articleTitle"));
            String articleTabloid = articleMap.get("articleTabloid");
            if(articleTabloid.length() <= 110){
                model.addAttribute("articleTabloid",articleTabloid);
            } else {
                model.addAttribute("articleTabloid",articleTabloid.substring(0,110));
            }
        }
        //将文章id存入响应头
        response.setHeader("articleId",String.valueOf(articleId));
        return "show";
    }
}
