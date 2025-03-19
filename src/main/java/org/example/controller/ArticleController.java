package org.example.controller;


import lombok.RequiredArgsConstructor;
import org.example.common.Result;
import org.example.sevice.ArticleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/list")
    public Result<String> list() {
        String result = articleService.list();

        return Result.success(result);
    }
}
