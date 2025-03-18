package org.example.sevice.Impl;

import org.example.annotation.LogOperation;
import org.example.sevice.ArticleService;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl implements ArticleService {
    @LogOperation(module = "文章管理", value = "查看文章列表")
    @Override
    public String list() {
        return "test";
    }
}
