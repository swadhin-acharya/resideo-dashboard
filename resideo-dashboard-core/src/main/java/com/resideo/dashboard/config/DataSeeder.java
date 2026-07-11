package com.resideo.dashboard.config;

import com.resideo.dashboard.model.entity.Organization;
import com.resideo.dashboard.model.entity.Project;
import com.resideo.dashboard.model.entity.ProjectMembership;
import com.resideo.dashboard.model.entity.UserEntity;
import com.resideo.dashboard.model.enums.GlobalRole;
import com.resideo.dashboard.model.enums.ProjectRole;
import com.resideo.dashboard.repository.OrganizationRepository;
import com.resideo.dashboard.repository.ProjectMembershipRepository;
import com.resideo.dashboard.repository.ProjectRepository;
import com.resideo.dashboard.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(OrganizationRepository organizationRepository,
                      ProjectRepository projectRepository,
                      UserRepository userRepository,
                      ProjectMembershipRepository membershipRepository,
                      PasswordEncoder passwordEncoder) {
        this.organizationRepository = organizationRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (organizationRepository.count() > 0) {
            return;
        }

        log.info("Seeding default data...");

        Organization org = new Organization();
        org.setName("Resideo");
        org.setSlug("resideo");
        org = organizationRepository.save(org);

        Project project = new Project();
        project.setOrganizationId(org.getId());
        project.setName("Default");
        project.setSlug("default");
        project.setDescription("Default project for existing data");
        project = projectRepository.save(project);

        UserEntity admin = new UserEntity();
        admin.setUsername("admin");
        admin.setEmail("admin@resideo.com");
        admin.setPasswordHash(passwordEncoder.encode("admin"));
        admin.setDisplayName("Admin");
        admin.setGlobalRole(GlobalRole.PLATFORM_ADMIN);
        admin.setEnabled(true);
        admin = userRepository.save(admin);

        ProjectMembership adminMembership = new ProjectMembership();
        adminMembership.setProjectId(project.getId());
        adminMembership.setUserId(admin.getId());
        adminMembership.setRole(ProjectRole.PROJECT_ADMIN);
        membershipRepository.save(adminMembership);

        UserEntity swadhin = new UserEntity();
        swadhin.setUsername("swadhin.acharya");
        swadhin.setEmail("swadhin.acharya@resideo.com");
        swadhin.setPasswordHash(passwordEncoder.encode("swadhin"));
        swadhin.setDisplayName("Swadhin Acharya");
        swadhin.setGlobalRole(GlobalRole.PLATFORM_ADMIN);
        swadhin.setEnabled(true);
        swadhin = userRepository.save(swadhin);

        ProjectMembership swadhinMembership = new ProjectMembership();
        swadhinMembership.setProjectId(project.getId());
        swadhinMembership.setUserId(swadhin.getId());
        swadhinMembership.setRole(ProjectRole.PROJECT_ADMIN);
        membershipRepository.save(swadhinMembership);

        log.info("Default organization, project, and admin user created.");
        log.info("Admin credentials: admin / admin");
        log.info("Swadhin credentials: swadhin.acharya / swadhin");
    }
}
