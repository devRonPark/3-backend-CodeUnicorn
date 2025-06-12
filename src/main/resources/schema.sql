create TABLE IF NOT EXISTS user(
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    email varchar(40) NOT NULL,
    nickname varchar(30) NOT NULL,
    platform_type varchar(6) NOT NULL,
    profile_path varchar(255) NULL,
    role varchar(5) DEFAULT "USER",
    ip varchar(32) NOT NULL,
    browser_type varchar(16) NOT NULL,
    created_at datetime DEFAULT current_timestamp,
    updated_at datetime DEFAULT NULL,
    deleted_at datetime DEFAULT NULL,
    PRIMARY KEY(id)
);
create TABLE IF NOT EXISTS user_access_log(
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    type varchar(15) NOT NULL,
    ip varchar(32) NOT NULL,
    browser_type varchar(16) NOT NULL,
    session_id varchar(255) NOT NULL,
    created_at datetime DEFAULT current_timestamp,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id)
    REFERENCES user(id) ON update CASCADE
);
create TABLE IF NOT EXISTS file_upload_log(
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    directory_path varchar(255) NOT NULL,
    name varchar(50) NOT NULL,
    type varchar(5) NOT NULL,
    size int NOT NULL,
    uploaded_at datetime DEFAULT current_timestamp,
    PRIMARY KEY (id)
);

create TABLE IF NOT EXISTS instructor(
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    name varchar(30) NOT NULL,
    work_experience varchar(255) NOT NULL,
    introduction varchar(255) NOT NULL,
    profile_path varchar(255) NULL,user
    PRIMARY KEY(id)
);
create TABLE IF NOT EXISTS course(
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    instructor_id INT UNSIGNED NOT NULL,
    category varchar(30),
    type tinyint DEFAULT 0,
    name varchar(50) NOT NULL,
    description varchar(255),
    price int DEFAULT 0,
    discount_rate tinyint DEFAULT 0,
    image_path varchar(255),
    view_count int DEFAULT 0,
    total_hours int DEFAULT 0,
    lecture_count int DEFAULT 0,
    average_ratings float DEFAULT 0.0,
    ratings_count int DEFAULT 0,
    user_count int DEFAULT 0,
    created_at datetime DEFAULT current_timestamp,
    updated_at datetime DEFAULT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (instructor_id)
    REFERENCES instructor(id) ON update CASCADE
);
create TABLE IF NOT EXISTS section(
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    course_id INT UNSIGNED NOT NULL,
    name varchar(30) NOT NULL,
    total_hours int DEFAULT 0,
    lecture_count int DEFAULT 0,
    PRIMARY KEY(id),
    FOREIGN KEY (course_id)
    REFERENCES course(id) ON update CASCADE
);
create TABLE IF NOT EXISTS lecture(
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    course_id INT UNSIGNED NOT NULL,
    section_id INT UNSIGNED NOT NULL,
    name varchar(30) NOT NULL,
    description varchar(50),
    video_url varchar(255) NOT NULL DEFAULT "",
    play_time int NOT NULL DEFAULT 0,
    PRIMARY KEY(id),
    FOREIGN KEY (course_id)
    REFERENCES course(id) ON update CASCADE,
    FOREIGN KEY (section_id)
    REFERENCES section(id) ON update CASCADE
);
create TABLE IF NOT EXISTS user_image(
    user_id INT UNSIGNED NOT NULL,
    file_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (user_id)
    REFERENCES user(id) ON update CASCADE,
    FOREIGN KEY (file_id)
    REFERENCES file_upload_log(id) ON update CASCADE
);
create TABLE IF NOT EXISTS instructor_image(
    instructor_id INT UNSIGNED NOT NULL,
    file_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (instructor_id)
    REFERENCES instructor(id) ON update CASCADE,
    FOREIGN KEY (file_id)
    REFERENCES file_upload_log(id) ON update CASCADE
);
create TABLE IF NOT EXISTS course_image(
    course_id INT UNSIGNED NOT NULL,
    file_id INT UNSIGNED NOT NULL,
    sequence TINYINT UNSIGNED NOT NULL,
    FOREIGN KEY (course_id)
    REFERENCES course(id) ON update CASCADE,
    FOREIGN KEY (file_id)
    REFERENCES file_upload_log(id) ON update CASCADE
);
