package com.hw.aggregate;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

public class Helper {
    public static String rStr() {
        return UUID.randomUUID().toString();
    }

    public static BigDecimal rBigDecimal() {
        int i = new Random().nextInt(10);
        return BigDecimal.valueOf(i);
    }

    public static Long rLong() {
        return new Random().nextLong();
    }

    public static String rJwt() {
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOiIwIiwiYXVkIjpbInByb2R1Y3QiLCJmaWxlLXVwbG9hZCIsImVkZ2UtcHJveHkiLCJ1c2VyLXByb2ZpbGUiLCJvYXV0aDItaWQiXSwidXNlcl9uYW1lIjoiMCIsInNjb3BlIjpbInRydXN0IiwicmVhZCIsIndyaXRlIl0sImV4cCI6MTU4NjM2MTExOSwiaWF0IjoxNTg2MzYwOTk5LCJhdXRob3JpdGllcyI6WyJST0xFX1JPT1QiLCJST0xFX0FETUlOIiwiUk9MRV9VU0VSIl0sImp0aSI6IjI1MGRlZWFmLWIxN2MtNDRmYy1iOGYzLWIzOGM0YjYyNjc2OSIsImNsaWVudF9pZCI6ImxvZ2luLWlkIn0.ZTujXwusFe6w_mWWEnoOZUHyodhaicfNLB7zKcNlnmGIkSNU6rochgJYw81w_M3z9-N9bsErUtSt8Gbp3O1VDQ-b3W-FbseEUh1M4ii64Iptww2jGTHJPXnCwLnfRzXIT4vwpctBZap94gAnKYm3k-brrtSzWqGRx77OP1CJuYYbyds73feLyA6SWFPpqbYdvFkl9FYyBg5YRPaHQm5_4V_YtWOz5ahE_QWcFdG5-jZrzesGA3qdCqS-9Bs__LnGGRA2ZIEwNwps9T3B-lnUyrucGPopWAnlXjmIg_M216md_NN--z2CBKT8iEfyDUc3RAFePJmDi_F8_V5P_UvohA";
    }

}
