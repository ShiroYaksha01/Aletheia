CREATE TABLE `user` (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE paper (
    paper_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    submission_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    author_id INT NOT NULL,
    CONSTRAINT fk_paper_author FOREIGN KEY (author_id) REFERENCES `user`(user_id)
);

CREATE TABLE co_author (
    paper_id INT NOT NULL,
    author_id INT NOT NULL,
    PRIMARY KEY (paper_id, author_id),
    CONSTRAINT fk_coauthor_paper FOREIGN KEY (paper_id) REFERENCES paper(paper_id),
    CONSTRAINT fk_coauthor_author FOREIGN KEY (author_id) REFERENCES `user`(user_id)
);

CREATE TABLE assignments (
    assignment_id INT PRIMARY KEY AUTO_INCREMENT,
    paper_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    assigned_by_id INT NOT NULL,
    assigned_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date DATE NOT NULL,
    CONSTRAINT fk_assignment_paper FOREIGN KEY (paper_id) REFERENCES paper(paper_id),
    CONSTRAINT fk_assignment_reviewer FOREIGN KEY (reviewer_id) REFERENCES `user`(user_id),
    CONSTRAINT fk_assignment_assigned_by FOREIGN KEY (assigned_by_id) REFERENCES `user`(user_id)
);

CREATE TABLE reviews (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    paper_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    comment TEXT,
    submitted_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_paper FOREIGN KEY (paper_id) REFERENCES paper(paper_id),
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES `user`(user_id)
);