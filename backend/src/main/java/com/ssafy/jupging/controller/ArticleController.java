package com.ssafy.jupging.controller;

import com.ssafy.jupging.domain.entity.Article;
import com.ssafy.jupging.domain.entity.User;
import com.ssafy.jupging.dto.ArticleResponseDto;
import com.ssafy.jupging.dto.ArticleSaveRequestDto;
import com.ssafy.jupging.dto.ArticleUpdateRequestDto;
import com.ssafy.jupging.dto.FollowResponseDto;
import com.ssafy.jupging.service.ArticleService;
import com.ssafy.jupging.service.CommentService;
import com.ssafy.jupging.service.MissionService;
import com.ssafy.jupging.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CrossOrigin(origins = { "*" }, maxAge = 6000)
@RequiredArgsConstructor
@RestController
@RequestMapping("/article")
public class ArticleController {

    private final ArticleService articleService;
    private final HashtagController hashtagController;
    private final MissionService missionService;
    private final UserService userService;

    private final CommentService commentService;

    /**
     * 게시글 등록
     * @param requestDto (content, user_id)
     * @return 등록된 게시글
     */
    @ApiOperation(value = "게시글 등록", notes = "성공 시 게시글 객체 반환 / 실패 시 에러메세지", response = ControllerResponse.class)
    @PostMapping
    public ControllerResponse saveArticle(@RequestBody ArticleSaveRequestDto requestDto) {
        ControllerResponse response = null;

        try {
            Article article = Article.saveArticle(requestDto);
            Article savedArticle =  articleService.save(article);

            //해시태그 저장
            hashtagController.saveHashtag(savedArticle.getContent(), savedArticle.getArticleId());

            //게시글미션 카운트+1
            missionService.updateArticleMission(requestDto.getUserId(), true);

            response = new ControllerResponse("success", article);
        } catch (Exception e) {
            response = new ControllerResponse("fail", e.getMessage());
        }

        return response;
    }

    /**
     * 게시글 상세 조회
     * @param article_id 게시글 번호
     * @return 게시글 내용
     */
    @ApiOperation(value = "게시글 상세 조회", notes = "성공 시 응답객체 반환 / 실패 시 에러메세지", response = ControllerResponse.class)
    @GetMapping("/detail/{article_id}")
    public ControllerResponse detailArticle(@PathVariable Long article_id){
        ControllerResponse response = null;

        try {
            Article article = articleService.findByArticleId(article_id);
            ArticleResponseDto articleResponseDto = new ArticleResponseDto(article);
            int commentCnt = commentService.countComment(articleResponseDto.getArticleId());
            articleResponseDto.setCommentCnt(commentCnt);

            User user = userService.findUser(articleResponseDto.getUserId());
            articleResponseDto.setNickname(user.getNickname());
            articleResponseDto.setProfilePath(user.getProfilePath());

            response = new ControllerResponse("success", articleResponseDto);
        } catch (Exception e) {
            response = new ControllerResponse("fail", e.getMessage());
        }

        return response;
    }

    @ApiOperation(value = "게시글 수정", notes = "성공 시 응답객체 반환 / 실패 시 에러메세지", response = ControllerResponse.class)
    @PutMapping
    public ControllerResponse updateArticle(@RequestBody ArticleUpdateRequestDto requestDto) {
        ControllerResponse response = null;

        try {
            articleService.updateArticle(requestDto);

            //수정된 게시글 리턴
            Article article = articleService.findByArticleId(requestDto.getArticleId());
            ArticleResponseDto articleResponseDto = new ArticleResponseDto(article);

            //해시태그 업데이트
            hashtagController.updateHashtag(articleResponseDto.getContent(), articleResponseDto.getArticleId());

            response = new ControllerResponse("success", articleResponseDto);
        } catch (Exception e) {
            response = new ControllerResponse("fail", e.getMessage());
        }
        return response;
    }

    @ApiOperation(value = "게시글 삭제", notes = "성공 시 '게시글 삭제 성공' 반환 / 실패 시 에러메세지", response = ControllerResponse.class)
    @DeleteMapping
    public ControllerResponse deleteArticle(@RequestParam Long articleId, @RequestParam Long userId){
        ControllerResponse response = null;
        try {
            hashtagController.deleteHashtag(articleId);//해시태그 삭제

            commentService.deleteAllComment(articleId); //댓글삭제

            articleService.deleteArticle(articleId);//게시글 삭제

            missionService.updateArticleMission(userId, false);//게시글 미션 카운트-1

            response = new ControllerResponse("success", "게시글 삭제 성공");
        }  catch (Exception e) {
            response = new ControllerResponse("fail", e.getMessage());
        }

        return response;
    }

    /**
     * 특정 회원의 게시글 목록을 받아오는 함수
     * @param user_id
     * @return
     */
    @ApiOperation(value = "유저가 쓴 게시글 찾기", notes = "성공시 게시글 리스트 반환 / 실패시 에러메세지", response = ControllerResponse.class)
    @GetMapping("/list/{user_id}")
    public ControllerResponse findUserArtice(@PathVariable Long user_id){
        ControllerResponse response = null;

        try{
            List<Article> articleList = articleService.findByUserId(user_id);
            List<ArticleResponseDto> responselist = new ArrayList<>();

            for(Article article:articleList){
                ArticleResponseDto articleResponseDto = new ArticleResponseDto(article);
                int commentCnt = commentService.countComment(articleResponseDto.getArticleId());
                articleResponseDto.setCommentCnt(commentCnt);

                User user = userService.findUser(articleResponseDto.getUserId());
                articleResponseDto.setNickname(user.getNickname());
                articleResponseDto.setProfilePath(user.getProfilePath());

                responselist.add(articleResponseDto);
            }
            if(responselist.isEmpty())
                response = new ControllerResponse("success", null);
            else response = new ControllerResponse("success", responselist);
        }
        catch (Exception e) {
            response = new ControllerResponse("fail", e.getMessage());
        }

        return response;
    }

    /**
     * 메인에 띄울 게시글을 반환해주는 함수
     * @return
     */
    @ApiOperation(value = "모든 게시글 찾기", notes = "메인에 띄울 모든 게시글 리스트 반환 / 실패 시 에러메세지 출력", response = ControllerResponse.class)
    @GetMapping("/list")
    public ControllerResponse findAllArticle() {
        ControllerResponse response = null;

        try {
            List<Article> articleList = articleService.findByOrderByCreatedDateDesc();
            List<ArticleResponseDto> responselist = new ArrayList<>();

            for(Article article:articleList){
                ArticleResponseDto articleResponseDto = new ArticleResponseDto(article);
                int commentCnt = commentService.countComment(articleResponseDto.getArticleId());
                articleResponseDto.setCommentCnt(commentCnt);

                User user = userService.findUser(articleResponseDto.getUserId());
                articleResponseDto.setNickname(user.getNickname());
                articleResponseDto.setProfilePath(user.getProfilePath());

                responselist.add(articleResponseDto);
            }
            response = new ControllerResponse("success", responselist);

        } catch (Exception e) {
            response = new ControllerResponse("fail", e.getMessage());
        }

        return response;
    }

}
