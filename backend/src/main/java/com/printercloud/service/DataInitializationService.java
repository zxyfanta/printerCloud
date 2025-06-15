package com.printercloud.service;

import com.printercloud.entity.PrintOrder;
import com.printercloud.entity.User;
import com.printercloud.repository.PrintOrderRepository;
import com.printercloud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 数据初始化服务
 * 在应用启动时创建一些示例数据
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Service
public class DataInitializationService {

    @Autowired
    private PrintOrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private Random random = new Random();

    /**
     * 初始化示例数据
     */
    @PostConstruct
    public void initSampleData() {
        try {
            // 检查是否已有数据，如果有则不初始化
            long orderCount = orderRepository.count();
            if (orderCount > 0) {
                System.out.println("数据库中已有 " + orderCount + " 条订单数据，跳过初始化");
                return;
            }

            System.out.println("开始初始化示例数据...");

            // 创建示例用户
            createSampleUsers();

            // 创建示例订单
            createSampleOrders();

            System.out.println("示例数据初始化完成");
        } catch (Exception e) {
            System.err.println("初始化示例数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建示例用户
     */
    private void createSampleUsers() {
        // 检查是否已有普通用户
        long userCount = userRepository.countByRole("USER");
        if (userCount > 0) {
            return;
        }

        String[] userNames = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十"};
        String[] phones = {"13800138001", "13800138002", "13800138003", "13800138004", 
                          "13800138005", "13800138006", "13800138007", "13800138008"};

        for (int i = 0; i < userNames.length; i++) {
            User user = new User();
            user.setOpenId("mock_openid_" + (i + 1));
            user.setNickname(userNames[i]);
            user.setPhone(phones[i]);
            user.setRole("USER");
            user.setStatus(0);
            user.setGender(random.nextInt(3)); // 0-未知，1-男，2-女
            userRepository.save(user);
        }

        System.out.println("已创建 " + userNames.length + " 个示例用户");
    }

    /**
     * 创建示例订单
     */
    private void createSampleOrders() {
        // 获取所有用户
        List<User> users = userRepository.findByRoleAndDeletedFalse("USER");
        if (users.isEmpty()) {
            System.out.println("没有找到普通用户，跳过创建示例订单");
            return;
        }


        String[] fileNames = {
            "工作报告.pdf", "彩色文档.pdf", "管理文档.docx", "会议纪要.pdf", 
            "项目计划.xlsx", "产品手册.pdf", "培训资料.pptx", "合同文件.pdf",
            "技术文档.pdf", "用户手册.docx", "财务报表.xlsx", "设计图纸.pdf"
        };

        String[] fileTypes = {"PDF", "PDF", "DOCX", "PDF", "XLSX", "PDF", "PPTX", "PDF", "PDF", "DOCX", "XLSX", "PDF"};
        String[] remarks = {
            "请在下午3点前完成", "彩色打印质量要好", "需要装订", "", 
            "双面打印节省纸张", "请用好一点的纸", "", "重要文件，请小心处理",
            "", "需要彩色打印", "", "图纸要清晰"
        };

        // 创建20个示例订单
        for (int i = 0; i < 20; i++) {
            PrintOrder order = new PrintOrder();
            
            // 随机选择用户
            User user = users.get(random.nextInt(users.size()));
            order.setUserId(user.getId());
            order.setUserName(user.getNickname());
            
            // 随机选择文件
            int fileIndex = random.nextInt(fileNames.length);
            order.setFileName(fileNames[fileIndex]);
            order.setFileType(fileTypes[fileIndex]);
            
            // 生成订单号和验证码
            order.setOrderNo(generateOrderNo());
            order.setVerifyCode(generateVerifyCode());
            
            // 随机打印配置
            order.setCopies(random.nextInt(3) + 1); // 1-3份
            order.setActualPages(random.nextInt(20) + 1); // 1-20页
            order.setIsColor(random.nextBoolean());
            order.setIsDoubleSide(random.nextBoolean());
            order.setPaperSize("A4");
            order.setPageRange("1-" + order.getActualPages());
            order.setRemark(remarks[fileIndex]);
            
            // 计算金额（简化计算）
            BigDecimal basePrice = order.getIsColor() ? new BigDecimal("0.8") : new BigDecimal("0.1");
            BigDecimal totalAmount = basePrice.multiply(new BigDecimal(order.getCopies() * order.getActualPages()));
            order.setAmount(totalAmount);
            
            // 随机状态
            int status = random.nextInt(5); // 0-4
            order.setStatus(status);
            
            // 设置时间
            LocalDateTime createTime = LocalDateTime.now().minusDays(random.nextInt(7)).minusHours(random.nextInt(24));
            order.setCreateTime(createTime);
            order.setUpdateTime(createTime);
            
            if (status >= 1) { // 已支付
                order.setPayTime(createTime.plusMinutes(random.nextInt(30)));
                order.setUpdateTime(order.getPayTime());
            }
            
            if (status >= 3) { // 已完成
                order.setFinishTime(order.getPayTime().plusHours(random.nextInt(6) + 1));
                order.setUpdateTime(order.getFinishTime());
            }
            
            orderRepository.save(order);
        }

        System.out.println("已创建 20 个示例订单");
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = String.format("%04d%02d%02d%02d%02d%02d", 
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                now.getHour(), now.getMinute(), now.getSecond());
        int randomNum = random.nextInt(9000) + 1000; // 1000-9999
        return "PC" + timestamp + randomNum;
    }

    /**
     * 生成验证码
     */
    private String generateVerifyCode() {
        return String.format("%06d", random.nextInt(900000) + 100000); // 100000-999999
    }
}
