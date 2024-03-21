package kr.co.sboard.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.sboard.dto.ArticleDTO;
import kr.co.sboard.dto.FileDTO;
import kr.co.sboard.dto.PageRequestDTO;
import kr.co.sboard.dto.PageResponseDTO;
import kr.co.sboard.service.ArticleService;
import kr.co.sboard.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.Console;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ArticleController {

    private final ArticleService articleService;
    private  final FileService fileService;
    /*
        @ModelAttribute("cate")
         - modelAttribute("cate", cate)와 동일
    */
    @GetMapping("/article/list")
    public String list(Model model, PageRequestDTO pageRequestDTO, @ModelAttribute("cate") String cate)
           {

        PageResponseDTO pageResponseDTO = articleService.findByParentAndCate(pageRequestDTO);
        model.addAttribute(pageResponseDTO);

        return "/article/list";
    }

    @GetMapping("/article/write")
    public String write(@ModelAttribute("cate") String cate){
        return "/article/write";
    }

    @PostMapping("/article/write")
    public String write(HttpServletRequest req, ArticleDTO articleDTO){
        /*
            글작성을 테스트할 때는 로그인해야하기 때문에
            SecurityConfig 인가 설정 수정할 것
        */
        String regip = req.getRemoteAddr();
        articleDTO.setRegip(regip);

        log.info(articleDTO.toString());

        articleService.insertArticle(articleDTO);

        return "redirect:/article/list?cate="+articleDTO.getCate();
    }

    @GetMapping("/article/view")
    public String view(int no, Model model){
        ArticleDTO articleDTO= articleService.selectArticle(no);
        model.addAttribute("articleDTO", articleDTO);

        List<ArticleDTO> comments = articleService.selectComment(no);
        log.info("comments "+comments);
        model.addAttribute("comments",comments);
        return "/article/view";
    }
    
    //추후에 pg 추가하기
    @GetMapping("/article/modify")
    public String modify(int no, Model model){
        ArticleDTO articleDTO = articleService.selectArticle(no);
        model.addAttribute("article", articleDTO);
        return "/article/modify";
    }


    //comment
    @PostMapping("/article/insertComment")
    public ResponseEntity insertComment(@RequestBody ArticleDTO commentDTO, HttpServletRequest request){
        commentDTO.setRegip(request.getRemoteAddr());
        log.info("info.. "+commentDTO);
        return articleService.inserComment(commentDTO);
    }

    @ResponseBody
    @DeleteMapping("/article/deleteComment/{no}")
    public ResponseEntity deleteComment(@PathVariable("no") int no){
     return   articleService.deleteComment(no);
    }

    @ResponseBody
    @PutMapping("/article/modifyComment")
    public ResponseEntity  modifyComment(@RequestBody ArticleDTO commentDTO){
        log.info("modify! "+commentDTO);
        ArticleDTO oldComment = articleService.selectCommentByNo(commentDTO.getNo());
        oldComment.setContent(commentDTO.getContent());

        return articleService.updateComment(oldComment);
    }

    @ResponseBody
    @GetMapping("/article/selectComment/{no}")
    public ResponseEntity  selectComment(@PathVariable("no") int no){
        ArticleDTO articleDTO =articleService.selectCommentByNo(no);
        Map<String , Object> map = new HashMap<>();
        map.put("article", articleDTO);
        return ResponseEntity.ok().body(map);
    }

}

