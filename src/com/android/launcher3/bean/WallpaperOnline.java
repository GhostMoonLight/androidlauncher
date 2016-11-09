package com.android.launcher3.bean;

import java.util.List;

/**
 * Created by cgx on 16/11/8.
 * 在线壁纸接口信息
 */

public class WallpaperOnline {

    /**
     * statusCode : 200
     * total : 67
     * results : [{"id":3196,"author":"","cover":"http://res.holaworld.cn/hola/wallpaper/preview/201610/10002205_s_e_540_480.jpg","source":1,"email":"","file":"http://res.holaworld.cn/hola/wallpaper/detail/201610/10002205_d_e_1440_1280.jpg","originSite":"","likes":419,"pkg":"10002205","len":3404,"code":"10002205","downloads":0},{"id":3193,"author":"","cover":"http://res.holaworld.cn/hola/wallpaper/preview/201610/10002202_s_e_540_480.jpg","source":1,"email":"","file":"http://res.holaworld.cn/hola/wallpaper/detail/201610/10002202_d_e_1440_1280.jpg","originSite":"","likes":499,"pkg":"10002202","len":19395,"code":"10002202","downloads":0},{"id":3190,"author":"","cover":"http://res.holaworld.cn/hola/wallpaper/preview/201610/10002199_s_e_540_480.jpg","source":1,"email":"","file":"http://res.holaworld.cn/hola/wallpaper/detail/201610/10002199_d_e_1440_1280.jpg","originSite":"","likes":740,"pkg":"10002199","len":8145,"code":"10002199","downloads":0},{"id":3170,"author":"","cover":"http://res.holaworld.cn/hola/wallpaper/preview/201610/10002179_s_e_540_480.jpg","source":1,"email":"","file":"http://res.holaworld.cn/hola/wallpaper/detail/201610/10002179_d_e_1440_1280.jpg","originSite":"","likes":620,"pkg":"10002179","len":4074,"code":"10002179","downloads":0},{"id":3162,"author":"","cover":"http://res.holaworld.cn/hola/wallpaper/preview/201609/0fdd1af82e89473f919f24ee19d596a4_preview_4.jpg","source":1,"email":"","file":"http://res.holaworld.cn/hola/wallpaper/detail/201609/10002172_d_e_1440_1280.jpg","originSite":"","likes":619,"pkg":"10002172","len":13883,"code":"10002172","downloads":0},{"id":3165,"author":"","cover":"http://res.holaworld.cn/hola/wallpaper/preview/201609/10002175_s_e_540_480.jpg","source":1,"email":"","file":"http://res.holaworld.cn/hola/wallpaper/detail/201609/10002175_d_e_1440_1280.jpg","originSite":"","likes":633,"pkg":"10002175","len":20247,"code":"10002175","downloads":0},{"id":3161,"author":"","cover":"http://res.holaworld.cn/hola/wallpaper/preview/201609/10002171_s_e_540_480.jpg","source":1,"email":"","file":"http://res.holaworld.cn/hola/wallpaper/detail/201609/10002171_d_e_1440_1280.jpg","originSite":"","likes":369,"pkg":"10002171","len":27174,"code":"10002171","downloads":0},{"id":3155,"author":"","cover":"http://res.holaworld.cn/hola/wallpaper/preview/201609/10002165_s_e_540_480.jpg","source":1,"email":"","file":"http://res.holaworld.cn/hola/wallpaper/detail/201609/10002165_d_e_1440_1280.jpg","originSite":"","likes":453,"pkg":"10002165","len":18902,"code":"10002165","downloads":0},{"id":3154,"author":"","cover":"http://res.holaworld.cn/hola/wallpaper/preview/201609/10002164_s_e_540_480.jpg","source":1,"email":"","file":"http://res.holaworld.cn/hola/wallpaper/detail/201609/10002164_d_e_1440_1280.jpg","originSite":"","likes":790,"pkg":"10002164","len":11169,"code":"10002164","downloads":0},{"id":3152,"author":"","cover":"http://res.holaworld.cn/hola/wallpaper/preview/201609/10002161_s_e_540_480.jpg","source":1,"email":"","file":"http://res.holaworld.cn/hola/wallpaper/detail/201609/10002161_d_e_1440_1280.jpg","originSite":"","likes":570,"pkg":"10002161","len":15906,"code":"10002161","downloads":0}]
     * page : 1
     * psize : 10
     * success : true
     * totalPage : 7
     */

    private int statusCode;
    private int total;
    private int page;
    private int psize;
    private boolean success;
    private int totalPage;
    /**
     * id : 3196
     * author :
     * cover : http://res.holaworld.cn/hola/wallpaper/preview/201610/10002205_s_e_540_480.jpg
     * source : 1
     * email :
     * file : http://res.holaworld.cn/hola/wallpaper/detail/201610/10002205_d_e_1440_1280.jpg
     * originSite :
     * likes : 419
     * pkg : 10002205
     * len : 3404
     * code : 10002205
     * downloads : 0
     */

    private List<WallpaperOnlineInfo> results;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPsize() {
        return psize;
    }

    public void setPsize(int psize) {
        this.psize = psize;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<WallpaperOnlineInfo> getResults() {
        return results;
    }

    public void setResults(List<WallpaperOnlineInfo> results) {
        this.results = results;
    }

    public static class WallpaperOnlineInfo {
        private int id;
        private String author;
        private String cover;
        private int source;
        private String email;
        private String file;
        private String originSite;
        private int likes;
        private String pkg;
        private int len;
        private String code;
        private int downloads;
        public int mHeight; //高度是否计算过
        public int mWeight;  //高度的权重

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getCover() {
            return cover;
        }

        public void setCover(String cover) {
            this.cover = cover;
        }

        public int getSource() {
            return source;
        }

        public void setSource(int source) {
            this.source = source;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getOriginSite() {
            return originSite;
        }

        public void setOriginSite(String originSite) {
            this.originSite = originSite;
        }

        public int getLikes() {
            return likes;
        }

        public void setLikes(int likes) {
            this.likes = likes;
        }

        public String getPkg() {
            return pkg;
        }

        public void setPkg(String pkg) {
            this.pkg = pkg;
        }

        public int getLen() {
            return len;
        }

        public void setLen(int len) {
            this.len = len;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public int getDownloads() {
            return downloads;
        }

        public void setDownloads(int downloads) {
            this.downloads = downloads;
        }

        //获取高度增加的权重   用来计算图片的瀑布流中item的高度
        public int getWeight(){
            if (mWeight==0){
                if (String.valueOf(len).length() > 1){
                    mWeight = (int) (len / Math.pow(10, String.valueOf(len).length()-1));
                }
            }
            return mWeight;
        }
    }
}
