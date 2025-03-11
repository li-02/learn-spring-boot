package org.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private static final String KEY = "zheshiyigehenchangdesecretnixinbuxin";
    // access_token过期时间, 3天
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 3;
    // refresh_token过期时间, 7天
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;
    // 用户ID的声明名称
    private static final String CLAIM_KEY_USER_ID = "userId";
    // 令牌类型的声明名称
    private static final String CLAIM_KEY_TOKEN_TYPE = "tokenType";
    // access token的类型
    private static final String TOKEN_TYPE_ACCESS = "access";
    // Refresh Token类型
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    /**
     * 生成access token
     *
     * @param userId
     * @return
     */
    public static String generateAccessToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USER_ID, userId);
        claims.put(CLAIM_KEY_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
        return genToken(claims, ACCESS_TOKEN_EXPIRATION);
    }

    /**
     * 生成刷新令牌
     *
     * @param userId 用户ID
     * @return 刷新令牌
     */
    public static String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USER_ID, userId);
        claims.put(CLAIM_KEY_TOKEN_TYPE, TOKEN_TYPE_REFRESH);
        return genToken(claims, REFRESH_TOKEN_EXPIRATION);
    }

    //接收业务数据,生成token并返回
    public static String genToken(Map<String, Object> claims, long expiration) {
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(Algorithm.HMAC256(KEY));
    }

	//接收token,验证token,并返回业务数据
    public static Map<String, Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }

    /**
     * 验证token是否有效
     *
     * @param token
     * @return
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
