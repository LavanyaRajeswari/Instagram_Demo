package com.web.Instagram.config;

import com.web.Instagram.entity.*;
import com.web.Instagram.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final MediaRepository mediaRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final HashtagRepository hashtagRepository;
    private final TagRepository tagRepository;
    private final SavedPostRepository savedPostRepository;
    private final CollectionRepository collectionRepository;
    private final ArchiveRepository archiveRepository;
    private final StoryRepository storyRepository;
    private final StoryLikeRepository storyLikeRepository;
    private final StoryViewRepository storyViewRepository;
    private final StoryReplyRepository storyReplyRepository;
    private final CloseFriendRepository closeFriendRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final MuteRepository muteRepository;
    private final RestrictionRepository restrictionRepository;
    private final NoteRepository noteRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReelAudioRepository reelAudioRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String[] FIRST_NAMES = {
        "Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Hank",
        "Ivy", "Jack", "Kate", "Leo", "Mia", "Noah", "Olivia", "Paul",
        "Quinn", "Ria", "Sam", "Tina"
    };

    private static final String[] LAST_NAMES = {
        "Smith", "Jones", "Lee", "Kim", "Brown", "Davis", "Wilson", "Moore",
        "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin",
        "Garcia", "Martinez", "Robinson", "Clark", "Lewis"
    };

    private static final String[] CAPTIONS = {
        "Beautiful sunset today! #sunset #nature",
        "Coffee and code #coding #coffee",
        "Weekend vibes! #weekend #fun",
        "New adventure begins #travel #adventure",
        "Good morning everyone! #morning #positive",
        "Beach day! #beach #summer",
        "City lights at night #city #night",
        "Hiking through the mountains #hiking #nature",
        "Fresh homemade pasta #food #cooking",
        "Art gallery visit #art #culture",
        "Fitness journey continues #fitness #gym",
        "Book club meeting #reading #books",
        "Garden update - tomatoes are growing! #garden #plants",
        "Concert last night was amazing #music #concert",
        "Rainy day vibes #rain #cozy",
        "Birthday celebration! #birthday #party",
        "New recipe tried and loved it! #foodie #recipe",
        "Sunrise yoga session #yoga #wellness",
        "Road trip day 1 #roadtrip #travel",
        "Pet love! #pets #dogs"
    };

    private static final String[] STORY_CAPTIONS = {
        "Good morning!",
        "Having fun!",
        "New day new vibes",
        "Check this out!",
        "Weekend mood",
        "Coffee time",
        "Sunset selfie",
        "With my bestie",
        "Workout done!",
        "Movie night"
    };

    private static final String[] COMMENTS = {
        "Amazing! 🔥",
        "Love this! ❤️",
        "Great shot!",
        "So beautiful!",
        "Where is this?",
        "Incredible!",
        "Nice one!",
        "Perfect!",
        "Can't believe this!",
        "My favorite!",
        "So cool!",
        "Love it!",
        "Beautiful!",
        "Fantastic!",
        "Wow!",
        "Stunning!",
        "Great vibes!",
        "Awesome!",
        "Keep it up!",
        "This is gold!"
    };

    private static final String[] HASHTAGS = {
        "sunset", "nature", "travel", "food", "fitness",
        "art", "music", "fashion", "love", "summer",
        "weekend", "coffee", "books", "garden", "yoga",
        "dogs", "cats", "photography", "happy", "life"
    };

    private static final String[] REEL_AUDIO_TITLES = {
        "Summer Vibes", "Night Groove", "Morning Energy", "Chill Beats",
        "Dance Mix", "LoFi Dreams", "Upbeat Pop", "RnB Soul"
    };

    private static final String[] REEL_AUDIO_ARTISTS = {
        "DJ Sunshine", "Night Owl", "Energy Boost", "Chillwave",
        "Party Master", "LoFi Guy", "Pop Star", "Soul Singer"
    };

    private static final String[] CHAT_MESSAGES = {
        "Hey! How are you?",
        "Great to hear from you!",
        "Want to grab coffee?",
        "Sure, sounds good!",
        "How about 3pm?",
        "Perfect! See you then",
        "Did you see the latest post?",
        "Yes, it's amazing!",
        "Let's plan a trip",
        "I'm in! Where should we go?"
    };

    public DataSeeder(
            UserRepository userRepository, PostRepository postRepository,
            MediaRepository mediaRepository, LikeRepository likeRepository,
            CommentRepository commentRepository, FollowRepository followRepository,
            HashtagRepository hashtagRepository, TagRepository tagRepository,
            SavedPostRepository savedPostRepository, CollectionRepository collectionRepository,
            ArchiveRepository archiveRepository, StoryRepository storyRepository,
            StoryLikeRepository storyLikeRepository, StoryViewRepository storyViewRepository,
            StoryReplyRepository storyReplyRepository, CloseFriendRepository closeFriendRepository,
            BlockedUserRepository blockedUserRepository, MuteRepository muteRepository,
            RestrictionRepository restrictionRepository, NoteRepository noteRepository,
            FavoriteRepository favoriteRepository, ReelAudioRepository reelAudioRepository,
            NotificationSettingRepository notificationSettingRepository,
            ChatRepository chatRepository, MessageRepository messageRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.mediaRepository = mediaRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.followRepository = followRepository;
        this.hashtagRepository = hashtagRepository;
        this.tagRepository = tagRepository;
        this.savedPostRepository = savedPostRepository;
        this.collectionRepository = collectionRepository;
        this.archiveRepository = archiveRepository;
        this.storyRepository = storyRepository;
        this.storyLikeRepository = storyLikeRepository;
        this.storyViewRepository = storyViewRepository;
        this.storyReplyRepository = storyReplyRepository;
        this.closeFriendRepository = closeFriendRepository;
        this.blockedUserRepository = blockedUserRepository;
        this.muteRepository = muteRepository;
        this.restrictionRepository = restrictionRepository;
        this.noteRepository = noteRepository;
        this.favoriteRepository = favoriteRepository;
        this.reelAudioRepository = reelAudioRepository;
        this.notificationSettingRepository = notificationSettingRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private <T> T randItem(T[] arr) {
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            System.out.println("Database already has data — skipping seed.");
            return;
        }

        System.out.println("Seeding database with test data...");
        long start = System.currentTimeMillis();

        List<User> users = new ArrayList<>();
        String encodedPassword = passwordEncoder.encode("password123");
        for (int i = 0; i < 20; i++) {
            String first = FIRST_NAMES[i];
            String last = LAST_NAMES[i];
            User u = new User();
            u.setUsername(first.toLowerCase() + last.toLowerCase());
            u.setFullName(first + " " + last);
            u.setEmail(first.toLowerCase() + "." + last.toLowerCase() + "@example.com");
            u.setPassword(encodedPassword);
            u.setBio("Hi, I'm " + first + "! " + randItem(CAPTIONS).replaceAll("#\\w+", "").trim());
            u.setGender(i % 2 == 0 ? "Male" : "Female");
            u.setProfilePicture("https://i.pravatar.cc/200?u=" + first);
            u.setIsPrivate(false);
            u.setIsVerified(i < 5);
            u.setRole("USER");
            u.setCreatedAt(LocalDateTime.now().minusDays(randInt(1, 365)));
            u.setUpdatedAt(LocalDateTime.now());
            users.add(u);
        }
        users = userRepository.saveAll(users);
        System.out.println("Created " + users.size() + " users");

        for (User u : users) {
            NotificationSetting ns = new NotificationSetting();
            ns.setUser(u);
            ns.setPushEnabled(true);
            ns.setLikesEnabled(true);
            ns.setCommentsEnabled(true);
            ns.setFollowsEnabled(true);
            ns.setMentionsEnabled(true);
            ns.setMessagesEnabled(true);
            ns.setStoriesEnabled(true);
            ns.setLiveEnabled(true);
            notificationSettingRepository.save(ns);
        }

        List<ReelAudio> reelAudios = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            ReelAudio a = new ReelAudio();
            a.setTitle(REEL_AUDIO_TITLES[i]);
            a.setArtist(REEL_AUDIO_ARTISTS[i]);
            a.setAudioUrl("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-" + (i + 1) + ".mp3");
            a.setCoverArtUrl("https://picsum.photos/seed/audio" + i + "/200/200");
            a.setDurationMs(ThreadLocalRandom.current().nextLong(120000, 300000));
            a.setGenre(randItem(new String[]{"Pop", "Rock", "Hip-Hop", "Jazz", "Electronic", "Classical"}));
            a.setIsTrending(i < 3);
            a.setUsageCount((long) randInt(100, 50000));
            a.setCreatedAt(LocalDateTime.now().minusDays(randInt(1, 90)));
            reelAudios.add(a);
        }
        reelAudios = reelAudioRepository.saveAll(reelAudios);
        System.out.println("Created " + reelAudios.size() + " reel audio tracks");

        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Post p = new Post();
            p.setUser(users.get(randInt(0, 19)));
            p.setCaption(CAPTIONS[i]);
            p.setVisibility("PUBLIC");
            p.setHideLikeCount(false);
            p.setCommentsDisabled(false);
            p.setCreatedAt(LocalDateTime.now().minusHours(randInt(1, 720)));
            p.setUpdatedAt(LocalDateTime.now());
            posts.add(p);
        }
        posts = postRepository.saveAll(posts);
        System.out.println("Created " + posts.size() + " posts");

        for (Post p : posts) {
            Media m = new Media();
            m.setPost(p);
            m.setMediaUrl("https://picsum.photos/seed/post" + p.getId() + "/640/640");
            m.setMediaType(MediaType.IMAGE);
            m.setPublicId("seed_post_" + p.getId());
            m.setSortOrder(0);
            mediaRepository.save(m);
        }
        System.out.println("Created media for posts");

        List<Post> reels = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Post r = new Post();
            r.setUser(users.get(randInt(0, 19)));
            r.setCaption("Reel " + (i + 1) + ": " + randItem(CAPTIONS).replaceAll("#\\w+", "").trim());
            r.setVisibility("PUBLIC");
            r.setHideLikeCount(false);
            r.setCommentsDisabled(false);
            r.setCreatedAt(LocalDateTime.now().minusHours(randInt(1, 720)));
            r.setUpdatedAt(LocalDateTime.now());
            reels.add(r);
        }
        reels = postRepository.saveAll(reels);
        System.out.println("Created " + reels.size() + " reels");

        for (Post r : reels) {
            Media m = new Media();
            m.setPost(r);
            m.setMediaUrl("https://www.w3schools.com/html/mov_bbb.mp4");
            m.setMediaType(MediaType.VIDEO);
            m.setPublicId("seed_reel_" + r.getId());
            m.setSortOrder(0);
            mediaRepository.save(m);
        }
        System.out.println("Created media for reels");

        int likeCount = 0;
        Set<String> existingLikes = new HashSet<>();
        for (Post p : posts) {
            int count = randInt(2, 10);
            for (int j = 0; j < count; j++) {
                User liker = users.get(randInt(0, 19));
                String key = liker.getId() + "_" + p.getId();
                if (existingLikes.contains(key)) continue;
                existingLikes.add(key);
                Like l = new Like();
                l.setUser(liker);
                l.setPost(p);
                l.setCreatedAt(LocalDateTime.now().minusHours(randInt(1, 168)));
                likeRepository.save(l);
                likeCount++;
            }
        }
        System.out.println("Created " + likeCount + " post likes");

        int reelLikeCount = 0;
        existingLikes.clear();
        for (Post r : reels) {
            int count = randInt(2, 10);
            for (int j = 0; j < count; j++) {
                User liker = users.get(randInt(0, 19));
                String key = liker.getId() + "_" + r.getId();
                if (existingLikes.contains(key)) continue;
                existingLikes.add(key);
                Like l = new Like();
                l.setUser(liker);
                l.setPost(r);
                l.setCreatedAt(LocalDateTime.now().minusHours(randInt(1, 168)));
                likeRepository.save(l);
                reelLikeCount++;
            }
        }
        System.out.println("Created " + reelLikeCount + " reel likes");

        int commentCount = 0;
        for (Post p : posts) {
            int count = randInt(1, 5);
            for (int j = 0; j < count; j++) {
                Comment c = new Comment();
                c.setPost(p);
                c.setUser(users.get(randInt(0, 19)));
                c.setText(randItem(COMMENTS));
                c.setLikeCount((long) randInt(0, 20));
                c.setLikedByCurrentUser(false);
                c.setCreatedAt(LocalDateTime.now().minusHours(randInt(1, 168)));
                c.setUpdatedAt(LocalDateTime.now());
                commentRepository.save(c);
                commentCount++;
            }
        }
        System.out.println("Created " + commentCount + " post comments");

        int reelCommentCount = 0;
        for (Post r : reels) {
            int count = randInt(1, 5);
            for (int j = 0; j < count; j++) {
                Comment c = new Comment();
                c.setPost(r);
                c.setUser(users.get(randInt(0, 19)));
                c.setText(randItem(COMMENTS));
                c.setLikeCount((long) randInt(0, 20));
                c.setLikedByCurrentUser(false);
                c.setCreatedAt(LocalDateTime.now().minusHours(randInt(1, 168)));
                c.setUpdatedAt(LocalDateTime.now());
                commentRepository.save(c);
                reelCommentCount++;
            }
        }
        System.out.println("Created " + reelCommentCount + " reel comments");

        int followCount = 0;
        existingLikes.clear();
        for (User follower : users) {
            int count = randInt(3, 15);
            for (int j = 0; j < count; j++) {
                User following = users.get(randInt(0, 19));
                if (follower.getId().equals(following.getId())) continue;
                String key = follower.getId() + "_" + following.getId();
                if (existingLikes.contains(key)) continue;
                existingLikes.add(key);
                Follow f = new Follow();
                f.setFollower(follower);
                f.setFollowing(following);
                f.setCreatedAt(LocalDateTime.now().minusDays(randInt(1, 180)));
                followRepository.save(f);
                followCount++;
            }
        }
        System.out.println("Created " + followCount + " follows");

        int hashtagCount = 0;
        Set<String> seenTags = new HashSet<>();
        for (Post p : posts) {
            String caption = p.getCaption();
            if (caption == null) continue;
            for (String word : caption.split("\\s+")) {
                if (word.startsWith("#")) {
                    String tag = word.substring(1).toLowerCase().replaceAll("[^a-z0-9]", "");
                    if (!tag.isEmpty() && !seenTags.contains(tag)) {
                        seenTags.add(tag);
                        Hashtag h = new Hashtag();
                        h.setTag(tag);
                        h.setPostId(p.getId());
                        hashtagRepository.save(h);
                        hashtagCount++;
                    }
                }
            }
        }
        System.out.println("Created " + hashtagCount + " hashtags");

        int tagCount = 0;
        for (Post p : posts) {
            int count = randInt(0, 3);
            Set<Long> tagged = new HashSet<>();
            for (int j = 0; j < count; j++) {
                User taggedUser = users.get(randInt(0, 19));
                if (taggedUser.getId().equals(p.getUser().getId())) continue;
                if (tagged.contains(taggedUser.getId())) continue;
                tagged.add(taggedUser.getId());
                Tag t = new Tag();
                t.setPost(p);
                t.setUser(taggedUser);
                t.setX(ThreadLocalRandom.current().nextDouble(0.1, 0.9));
                t.setY(ThreadLocalRandom.current().nextDouble(0.1, 0.9));
                t.setCreatedAt(LocalDateTime.now());
                tagRepository.save(t);
                tagCount++;
            }
        }
        System.out.println("Created " + tagCount + " tags");

        int savedCount = 0;
        existingLikes.clear();
        for (User u : users) {
            int count = randInt(1, 5);
            for (int j = 0; j < count; j++) {
                Post p = posts.get(randInt(0, 19));
                String key = u.getId() + "_" + p.getId();
                if (existingLikes.contains(key)) continue;
                existingLikes.add(key);
                SavedPost sp = new SavedPost();
                sp.setUser(u);
                sp.setPost(p);
                sp.setCreatedAt(LocalDateTime.now().minusDays(randInt(1, 60)));
                savedPostRepository.save(sp);
                savedCount++;
            }
        }
        System.out.println("Created " + savedCount + " saved posts");

        int collectionCount = 0;
        for (User u : users.subList(0, 10)) {
            int count = randInt(1, 3);
            for (int j = 0; j < count; j++) {
                com.web.Instagram.entity.Collection c = new com.web.Instagram.entity.Collection();
                c.setName(randItem(new String[]{"Favorites", "Travel", "Food", "Nature", "Art", "Inspiration", "Books", "Fitness"}));
                c.setUser(u);
                List<Post> colPosts = new ArrayList<>();
                int pc = randInt(1, 5);
                for (int k = 0; k < pc; k++) {
                    colPosts.add(posts.get(randInt(0, 19)));
                }
                c.setPosts(colPosts);
                c.setCreatedAt(LocalDateTime.now().minusDays(randInt(1, 30)));
                c.setUpdatedAt(LocalDateTime.now());
                collectionRepository.save(c);
                collectionCount++;
            }
        }
        System.out.println("Created " + collectionCount + " collections");

        int archiveCount = 0;
        for (int i = 0; i < 10; i++) {
            Post p = posts.get(i);
            if (p.getUser() != null) {
                Archive a = new Archive();
                a.setUser(p.getUser());
                a.setPost(p);
                a.setArchivedAt(LocalDateTime.now().minusDays(randInt(1, 30)));
                archiveRepository.save(a);
                archiveCount++;
            }
        }
        System.out.println("Created " + archiveCount + " archives");

        int storyCount = 0;
        for (User u : users) {
            int count = randInt(2, 3);
            for (int j = 0; j < count; j++) {
                Story s = new Story();
                s.setUser(u);
                s.setMediaUrl("https://picsum.photos/seed/story" + u.getId() + "_" + j + "/400/700");
                s.setMediaType("IMAGE");
                s.setCaption(j == 0 ? randItem(STORY_CAPTIONS) : null);
                s.setCreatedAt(LocalDateTime.now().minusHours(randInt(1, 20)));
                s.setExpiresAt(LocalDateTime.now().plusHours(randInt(1, 24)));
                s.setAudience("PUBLIC");
                storyRepository.save(s);
                storyCount++;
            }
        }
        System.out.println("Created " + storyCount + " stories");

        int storyLikeCount = 0;
        Set<String> storyLikeDedup = new HashSet<>();
        List<Story> allStories = storyRepository.findAll();
        for (Story s : allStories) {
            int count = randInt(0, 8);
            for (int j = 0; j < count; j++) {
                User liker = users.get(randInt(0, 19));
                String key = s.getId() + "_" + liker.getId();
                if (storyLikeDedup.contains(key)) continue;
                storyLikeDedup.add(key);
                StoryLike sl = new StoryLike();
                sl.setStory(s);
                sl.setUser(liker);
                sl.setCreatedAt(LocalDateTime.now().minusHours(randInt(1, 12)));
                storyLikeRepository.save(sl);
                storyLikeCount++;
            }
        }
        System.out.println("Created " + storyLikeCount + " story likes");

        int storyViewCount = 0;
        Set<String> storyViewDedup = new HashSet<>();
        for (Story s : allStories) {
            int count = randInt(1, 10);
            for (int j = 0; j < count; j++) {
                User viewer = users.get(randInt(0, 19));
                String key = s.getId() + "_" + viewer.getId();
                if (storyViewDedup.contains(key)) continue;
                storyViewDedup.add(key);
                StoryView sv = new StoryView();
                sv.setStory(s);
                sv.setUser(viewer);
                storyViewRepository.save(sv);
                storyViewCount++;
            }
        }
        System.out.println("Created " + storyViewCount + " story views");

        int storyReplyCount = 0;
        for (Story s : allStories.subList(0, Math.min(10, allStories.size()))) {
            int count = randInt(0, 3);
            for (int j = 0; j < count; j++) {
                User replier = users.get(randInt(0, 19));
                StoryReply sr = new StoryReply();
                sr.setStory(s);
                sr.setUser(replier);
                sr.setText(randItem(COMMENTS));
                sr.setCreatedAt(LocalDateTime.now().minusHours(randInt(1, 12)));
                storyReplyRepository.save(sr);
                storyReplyCount++;
            }
        }
        System.out.println("Created " + storyReplyCount + " story replies");

        int closeFriendCount = 0;
        Set<String> cfDedup = new HashSet<>();
        for (User u : users) {
            int count = randInt(1, 5);
            for (int j = 0; j < count; j++) {
                User friend = users.get(randInt(0, 19));
                if (u.getId().equals(friend.getId())) continue;
                String key = u.getId() + "_" + friend.getId();
                if (cfDedup.contains(key)) continue;
                cfDedup.add(key);
                CloseFriend cf = new CloseFriend();
                cf.setUser(u);
                cf.setFriend(friend);
                cf.setCreatedAt(LocalDateTime.now().minusDays(randInt(1, 60)));
                closeFriendRepository.save(cf);
                closeFriendCount++;
            }
        }
        System.out.println("Created " + closeFriendCount + " close friend entries");

        int blockedCount = 0;
        Set<String> blockedDedup = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            User blocker = users.get(i);
            User blockedUser = users.get(randInt(10, 19));
            String key = blocker.getId() + "_" + blockedUser.getId();
            if (blockedDedup.contains(key)) continue;
            blockedDedup.add(key);
            BlockedUser bu = new BlockedUser();
            bu.setBlocker(blocker);
            bu.setBlocked(blockedUser);
            bu.setCreatedAt(LocalDateTime.now().minusDays(randInt(1, 30)));
            blockedUserRepository.save(bu);
            blockedCount++;
        }
        System.out.println("Created " + blockedCount + " blocked user entries");

        int muteCount = 0;
        Set<String> muteDedup = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            User muter = users.get(i);
            User mutedTarget = users.get(randInt(10, 19));
            String key = muter.getId() + "_" + mutedTarget.getId() + "_ALL";
            if (muteDedup.contains(key)) continue;
            muteDedup.add(key);
            Mute m = new Mute();
            m.setUser(muter);
            m.setMutedUser(mutedTarget);
            m.setMuteType("ALL");
            m.setCreatedAt(LocalDateTime.now().minusDays(randInt(1, 30)));
            muteRepository.save(m);
            muteCount++;
        }
        System.out.println("Created " + muteCount + " mute entries");

        int restrictCount = 0;
        Set<String> restrictDedup = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            User restricter = users.get(i);
            User restrictedTarget = users.get(randInt(10, 19));
            String key = restricter.getId() + "_" + restrictedTarget.getId();
            if (restrictDedup.contains(key)) continue;
            restrictDedup.add(key);
            Restriction r = new Restriction();
            r.setUser(restricter);
            r.setRestrictedUser(restrictedTarget);
            r.setCreatedAt(LocalDateTime.now().minusDays(randInt(1, 30)));
            restrictionRepository.save(r);
            restrictCount++;
        }
        System.out.println("Created " + restrictCount + " restriction entries");

        int noteCount = 0;
        for (User u : users.subList(0, 10)) {
            Note n = new Note();
            n.setUser(u);
            n.setText(randItem(new String[]{"Feeling good today!", "Busy week ahead", "Anyone want to hang out?", "New music recommendation!", "Just finished a great book"}));
            n.setColor(randItem(new String[]{"PINK", "GRADIENT_PINK", "GRADIENT_PURPLE", "GRADIENT_COSMIC"}));
            n.setAudience("FOLLOWERS");
            n.setCreatedAt(LocalDateTime.now().minusHours(randInt(1, 48)));
            n.setExpiresAt(LocalDateTime.now().plusHours(randInt(1, 24)));
            noteRepository.save(n);
            noteCount++;
        }
        System.out.println("Created " + noteCount + " notes");

        int favoriteCount = 0;
        for (User u : users.subList(0, 5)) {
            int count = randInt(1, 5);
            for (int j = 0; j < count; j++) {
                Favorite f = new Favorite();
                f.setUser(u);
                f.setPost(posts.get(randInt(0, 19)));
                f.setCreatedAt(LocalDateTime.now().minusDays(randInt(1, 30)));
                favoriteRepository.save(f);
                favoriteCount++;
            }
        }
        System.out.println("Created " + favoriteCount + " favorites");

        int chatCount = 0;
        for (int i = 0; i < 10; i++) {
            User u1 = users.get(i);
            User u2 = users.get(i + 10);
            Chat c = new Chat();
            c.setUserOne(u1);
            c.setUserTwo(u2);
            c.setCreatedAt(LocalDateTime.now().minusDays(randInt(1, 60)));
            chatRepository.save(c);
            chatCount++;

            int msgCount = randInt(2, 6);
            for (int j = 0; j < msgCount; j++) {
                Message msg = new Message();
                msg.setChat(c);
                msg.setSender(j % 2 == 0 ? u1 : u2);
                msg.setContent(randItem(CHAT_MESSAGES));
                msg.setMessageType("TEXT");
                msg.setCreatedAt(LocalDateTime.now().minusHours(randInt(1, 168)));
                msg.setSeen(j < msgCount - 1);
                messageRepository.save(msg);
            }
        }
        System.out.println("Created " + chatCount + " chats with messages");

        int audioUsageCount = 0;
        List<Post> allReelsInDb = postRepository.findAll().stream()
                .filter(p -> p.getMedia().stream().anyMatch(m -> m.getMediaType() == MediaType.VIDEO))
                .toList();
        for (Post reel : allReelsInDb) {
            if (randInt(0, 3) > 0) {
                String caption = reel.getCaption();
                if (caption != null && !caption.isEmpty()) {
                    caption += " 🎵 " + randItem(REEL_AUDIO_TITLES);
                    reel.setCaption(caption);
                    postRepository.save(reel);
                    audioUsageCount++;
                }
            }
        }
        System.out.println("Linked " + audioUsageCount + " reels to audio");

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("✅ Database seeding completed in " + elapsed + "ms");
        System.out.println("  Users: 20, Posts: 20, Reels: 20");
        System.out.println("  Follows: " + followCount + ", Post Likes: " + likeCount + ", Reel Likes: " + reelLikeCount);
        System.out.println("  Post Comments: " + commentCount + ", Reel Comments: " + reelCommentCount);
        System.out.println("  Stories: " + storyCount + ", Hashtags: " + hashtagCount + ", Tags: " + tagCount);
        System.out.println("  Saved Posts: " + savedCount + ", Collections: " + collectionCount);
        System.out.println("  Close Friends: " + closeFriendCount + ", Blocked: " + blockedCount);
        System.out.println("  Mutes: " + muteCount + ", Restrictions: " + restrictCount);
        System.out.println("  Notes: " + noteCount + ", Favorites: " + favoriteCount);
        System.out.println("  Chats: " + chatCount + ", Archives: " + archiveCount);
        System.out.println("  Story Likes: " + storyLikeCount + ", Story Views: " + storyViewCount + ", Story Replies: " + storyReplyCount);
    }
}
