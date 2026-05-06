CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   username VARCHAR(255) UNIQUE,
   encrypted_password VARCHAR(255) NOT NULL,
   profile_icon_image_key VARCHAR(255)
);

CREATE TABLE posts (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   title VARCHAR(255) NOT NULL,
   body TEXT NOT NULL,
   category VARCHAR(50) NOT NULL,
   like_count INT NOT NULL DEFAULT 0,
   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
   author_id UUID NOT NULL,

   CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    body VARCHAR(1000) NOT NULL,
    post_id UUID NOT NULL,
    author_id UUID NOT NULL,
    parent_comment_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE
);

CREATE TABLE likes (
   id BIGSERIAL PRIMARY KEY,
   user_id UUID NOT NULL,
   post_id UUID NOT NULL,

   CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
   CONSTRAINT fk_likes_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
   CONSTRAINT unique_user_post_like UNIQUE (user_id, post_id)
);