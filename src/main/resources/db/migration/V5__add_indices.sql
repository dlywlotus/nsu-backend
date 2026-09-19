-- Indices for posts table
CREATE INDEX IF NOT EXISTS idx_posts_author_id ON posts USING btree (author_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts USING btree (created_at);

-- Indices for comments table
CREATE INDEX IF NOT EXISTS idx_comments_post_id ON comments USING btree (post_id);