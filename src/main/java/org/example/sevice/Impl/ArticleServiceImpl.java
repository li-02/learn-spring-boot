package org.example.sevice.Impl;

import org.example.sevice.ArticleService;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Override
    public String list() {
        return "test";
    }
}
