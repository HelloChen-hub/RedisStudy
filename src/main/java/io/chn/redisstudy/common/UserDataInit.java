package io.chn.redisstudy.common;

import io.chn.redisstudy.entity.SysRole;
import io.chn.redisstudy.entity.User;
import io.chn.redisstudy.service.SysRoleService;
import io.chn.redisstudy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
public class UserDataInit implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private SysRoleService sysRoleService;

    private static final String[] ROLES = {"ADMIN", "MANAGER", "EDITOR", "USER", "GUEST"};
    private static final int[] WEIGHTS = {1, 5, 10, 80, 4};

    private static final String[] SURNAMES = {
            "zhang", "wang", "li", "zhao", "liu", "chen", "yang", "huang", "zhou", "wu",
            "xu", "sun", "hu", "zhu", "gao", "lin", "he", "guo", "ma", "luo",
            "liang", "song", "zheng", "xie", "han", "tang", "feng", "yu", "dong", "xiao"
    };

    private static final String[] GIVEN_NAMES = {
            "wei", "fang", "jun", "min", "jing", "lei", "na", "qiang", "yan", "juan",
            "tao", "ming", "chao", "hong", "gang", "ping", "ling", "hui", "xin", "yi",
            "cheng", "hao", "yu", "xiang", "long", "fei", "bo", "kai", "rui", "ting"
    };

    @Override
    public void run(String... args) {
        // 若表里已有数据，跳过；不需要可注释掉
        if (userService.count() > 0) {
            System.out.println("user 表已有数据，跳过初始化");
            return;
        }
        init();
    }

    public void init() {
        ensureRoles();
        generateUsers(100000, 1000);
    }

    /**
     * 确保 sys_role 表里有需要的角色
     */
    private void ensureRoles() {
        String[][] roles = {
                {"ADMIN", "管理员"},
                {"MANAGER", "经理"},
                {"EDITOR", "编辑"},
                {"USER", "普通用户"},
                {"GUEST", "访客"}
        };
        for (String[] r : roles) {
            boolean exists = sysRoleService.lambdaQuery()
                    .eq(SysRole::getRoleCode, r[0])
                    .exists();
            if (!exists) {
                SysRole role = new SysRole();
                role.setRoleCode(r[0]);
                role.setRoleName(r[1]);
                sysRoleService.save(role);
            }
        }
    }

    /**
     * 生成并批量插入用户
     */
    private void generateUsers(int total, int batchSize) {
        Random random = new Random();
        Set<String> usedUsernames = new HashSet<>(total);
        List<User> buffer = new ArrayList<>(batchSize);

        long start = System.currentTimeMillis();

        for (int i = 0; i < total; i++) {
            User user = new User();

            // 唯一用户名
            String username;
            do {
                String surname = SURNAMES[random.nextInt(SURNAMES.length)];
                String givenName = GIVEN_NAMES[random.nextInt(GIVEN_NAMES.length)];
                int suffix = random.nextInt(100000);
                username = surname + givenName + String.format("%05d", suffix);
            } while (!usedUsernames.add(username));
            user.setUsername(username);

            user.setPhone(generatePhone(random));
            user.setCreateTime(randomCreateTime(random));
            user.setRoleCode(selectRole(random));

            buffer.add(user);

            if (buffer.size() == batchSize) {
                userService.saveBatch(buffer, batchSize);
                buffer.clear();
                System.out.println("已插入 " + (i + 1) + " 条");
            }
        }

        if (!buffer.isEmpty()) {
            userService.saveBatch(buffer, batchSize);
        }

        long cost = System.currentTimeMillis() - start;
        System.out.println("全部完成，共 " + total + " 条，耗时 " + cost + " ms");
    }

    /**
     * 生成随机手机号：1[3-9] + 9 位数字
     */
    private String generatePhone(Random random) {
        StringBuilder sb = new StringBuilder("1");
        sb.append(random.nextInt(7) + 3); // 第二位 3~9
        for (int i = 0; i < 9; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成过去 3 年内的随机时间
     */
    private LocalDateTime randomCreateTime(Random random) {
        return LocalDateTime.now()
                .minusDays(random.nextInt(1095))
                .minusHours(random.nextInt(24))
                .minusMinutes(random.nextInt(60))
                .minusSeconds(random.nextInt(60));
    }

    /**
     * 按权重随机选角色
     */
    private String selectRole(Random random) {
        int totalWeight = 0;
        for (int w : WEIGHTS) totalWeight += w;
        int r = random.nextInt(totalWeight);
        int cumulative = 0;
        for (int i = 0; i < ROLES.length; i++) {
            cumulative += WEIGHTS[i];
            if (r < cumulative) return ROLES[i];
        }
        return ROLES[ROLES.length - 1];
    }
}
