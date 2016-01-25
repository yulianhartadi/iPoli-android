package io.ipoli.android.player;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/24/16.
 */
public class Reward {

    public static Reward[] REWARDS = {
            new Reward("https://s-media-cache-ak0.pinimg.com/736x/8d/5c/98/8d5c98c68faf0d66da74211d416a37c1.jpg", "lolzombie.com"),
            new Reward("http://cdn.gagbay.com/2013/07/i_swear_honey_im_just_as_surprised_as_you-314808.jpg", "gagbay.com"),
            new Reward("http://www.brainyquote.com/photos/r/richardpfeynman137642.jpg", "brainyquote.com"),
            new Reward("http://memeszone.com/wp-content/uploads/2015/04/lounging-funny-memes-cats.jpg", "memeszone.com"),
            new Reward("http://cdn.grumpycats.com/wp-content/uploads/2012/10/I-Had-Fun-Once-It-Was-Awful.jpeg", "grumpycats.com")
    };

    private String url;
    private String source;

    public Reward(String url, String source) {
        this.url = url;
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public String getSource() {
        return source;
    }
}
