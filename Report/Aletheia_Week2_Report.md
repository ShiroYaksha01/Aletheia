# Week 2 Report


1. Commits on Dec 15, 2025
   - Kimheng:
       - config build.gradle ![alt](images/build2.png)



2. Commits on Dec 16, 2025
   - Kimheng:
       - Adding "FlywayConifg.java"
       - stop using ```spring.jpa.hibernate.ddl-auto=none``` and organize the `src/main/resources/application.properties` file into sections ![prop sections](images/section.png)
       - Changed some syntax inside `src/main/resources/db/migration/V1__first_db_design.sql` Example From \`user\` to users
    - Setha:
        - Accepting pull request to merge with main branch from Kimheng


3. Commits on Dec 17, 2025

   - Pheaktra:
       - Add security, Register and Login logic by using the concepts we learned from our TP lessons
       - Adding springboot security and jwt dependencies in `build.gradle`
       - The files created are:
           - AuthRepsonse.java
           - AuthenticationController.java
           - CorsConfig.java
           - CustomUserDetailsService.java
           - JwtFilter.java
           - JwtProperties.java
           - JwtUtil.java
           - LoginRequest.java
           - LoginResponse.java
           - SecurityConfig.java
           - UserEntity.java
           - UserRepository.java
       - Adding security properties ![security properties](images/security.png)



4. Commits on Dec 18, 2025

   - Kity:
       - organize the src folders structure from everything inside the `src/main/java/aletheia/project/Aletheia/` folder into multiple folders for thier specific purposes, like config folder, controller folder, dto folder, etc.
       - Creating `WebContoller.java` and UI for "dashboard", "login", and "signup" pages.
   - Setha: 
       - Accepting pull request to merge from Pheaktra.
       - Editing README.md file to show the project objectives.
       - Editing `Report/Aletheia_Week1_Report.md` to show week 1 progress in simple sentences.
       - Accepting pull request to merge from Kity (He accidentally delete the report folder because he thought it might be the causes for build error).
       - Adding Report folders back alongside week2 report


5. Commits on Dec 21, 2025
   - Setha:
       - Edited Week 2 report to add Pheaktra and Kity activity on the report
   - Kimheng:
       - Added his own commit on week 2 report progress
       - Created Diagram folder inside the report folder which included `activty_diagram.puml`, `login-register_activity_diagram.puml`, and `use_case.puml`.
       - Created `PaperEntity.java` file


6. Commits on Dec 22, 2025
   - Kimheng:
       - feat: Add paper submission system with entity, service, controller and UI
         - Create complete V1 migration with papers table (BIGINT id, abstract_text, file_name, created_at)
         - Implement PaperEntity, PaperRepository, and PaperService
         - Build PaperController with paper creation endpoints
       - fix files ReviewRepository and AssigmentRepository reviewerID -> reviewer.id

   - Phayuk:
       - created repositories, entities, and drafted controller (no logic yet) for 
         - Assignment
         - CoAuthor
         - CoAuthorId
         - Paper (This one conflicted with Kimheng commit)
         - Review
   - Setha:
       - Accepted merge pull request from Kimheng
       - Fixing conflict between Kimheng and Phayuk commit (Both of them done on Paper repositories, entities, and controller), and i choose kimheng commit as Kimheng had finished All of paper controller, entities and repositories
       - Accepted merge pull request from Phayuk
       - Accepted merge pull request from Kity
   - Kity:
       - Fix securityConfig bug

            ```java
                        .authenticationProvider(authProvider())
                        .authorizeHttpRequests(auth -> auth
                            // 1. Only allow these without login
                            .requestMatchers("/login", "/register").permitAll()
                            .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                            // 2. Everything else requires authentication
                            .anyRequest().authenticated()
            ```

            to 

            ```java
                        .authenticationProvider(authProvider())
                        .authorizeHttpRequests(auth -> auth
                            // 1. Only allow these without login
                            .requestMatchers("/login", "/register", "/login-process", "/register-process").permitAll()
                            .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                            // 2. Everything else requires authentication
                            .anyRequest().authenticated()
            ```


