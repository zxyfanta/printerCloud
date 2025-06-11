package com.printercloud.config;

import com.printercloud.entity.PrintFile;
import com.printercloud.entity.PrintOrder;
import com.printercloud.entity.PriceConfig;
import com.printercloud.entity.User;
import com.printercloud.repository.PrintFileRepository;
import com.printercloud.repository.PrintOrderRepository;
import com.printercloud.repository.PriceConfigRepository;
import com.printercloud.repository.UserRepository;
import com.printercloud.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * 数据初始化器
 * 
 * @author PrinterCloud
 * @since 2024-01-01
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PrintOrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PrintFileRepository fileRepository;

    @Autowired
    private PriceConfigRepository priceConfigRepository;

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有数据
        if (orderRepository.count() > 0) {
            return;
        }

        // 创建测试数据
        createDefaultPriceConfigs();
        createDefaultSystemConfigs();
        createTestUsers();
        createTestFiles();
        createTestOrders();

        System.out.println("测试数据初始化完成！");
    }

    private void createDefaultPriceConfigs() {
        // 检查是否已有价格配置
        if (priceConfigRepository.count() > 0) {
            return;
        }

        // 创建默认价格配置
        PriceConfig bwPrint = new PriceConfig("bw_print", "黑白打印", new BigDecimal("0.10"), "元/页");
        bwPrint.setDescription("黑白文档打印价格");
        bwPrint.setSortOrder(1);
        priceConfigRepository.save(bwPrint);

        PriceConfig colorPrint = new PriceConfig("color_print", "彩色打印", new BigDecimal("0.50"), "元/页");
        colorPrint.setDescription("彩色文档打印价格");
        colorPrint.setSortOrder(2);
        priceConfigRepository.save(colorPrint);

        PriceConfig doubleSideDiscount = new PriceConfig("double_side_discount", "双面打印折扣", new BigDecimal("0.80"), "折扣率");
        doubleSideDiscount.setDescription("双面打印享受8折优惠");
        doubleSideDiscount.setSortOrder(3);
        priceConfigRepository.save(doubleSideDiscount);

        PriceConfig a3Extra = new PriceConfig("a3_extra", "A3纸张加价", new BigDecimal("0.20"), "元/页");
        a3Extra.setDescription("A3纸张额外费用");
        a3Extra.setSortOrder(4);
        priceConfigRepository.save(a3Extra);

        System.out.println("默认价格配置创建完成");
    }

    private void createDefaultSystemConfigs() {
        // 初始化默认系统配置
        systemConfigService.initDefaultConfigs();
        System.out.println("默认系统配置创建完成");
    }

    private void createTestUsers() {
        // 创建普通用户
        for (int i = 1; i <= 10; i++) {
            User user = new User();
            user.setOpenId("test_openid_" + i);
            user.setNickname("测试用户" + i);
            user.setPhone("1380013800" + String.format("%02d", i));
            user.setRole("USER");
            user.setStatus(0);
            userRepository.save(user);
        }
    }

    private void createTestFiles() {
        Random random = new Random();
        String[] fileNames = {
            "工作报告.pdf", "会议纪要.docx", "数据分析.xlsx",
            "项目方案.pptx", "合同文件.pdf", "学习资料.pdf",
            "产品手册.docx", "财务报表.xlsx", "演示文稿.pptx", "技术文档.pdf",
            "用户手册.pdf", "设计图纸.dwg", "培训材料.pptx", "年度总结.docx",
            "预算表.xlsx"
        };

        String[] fileTypes = {
            "application/pdf", "application/msword",
            "application/vnd.ms-excel", "application/vnd.ms-powerpoint"
        };

        for (int i = 0; i < fileNames.length; i++) {
            PrintFile file = new PrintFile();
            file.setUserId((long) (random.nextInt(10) + 3)); // 用户ID 3-12 (跳过管理员)
            file.setOriginalName(fileNames[i]);
            file.setFileName("file_" + System.currentTimeMillis() + "_" + i + getFileExtension(fileNames[i]));
            file.setFilePath("/uploads/" + file.getFileName());
            file.setFileSize((long) (random.nextInt(5000000) + 100000)); // 100KB-5MB
            file.setFileType(fileTypes[random.nextInt(fileTypes.length)]);
            file.setFileMd5("md5_" + System.currentTimeMillis() + "_" + i);
            file.setPageCount(random.nextInt(50) + 1); // 1-50页
            file.setStatus(1); // 上传成功
            file.setCreateTime(LocalDateTime.now().minusDays(random.nextInt(30)));
            fileRepository.save(file);
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : ".pdf";
    }

    private void createTestOrders() {
        Random random = new Random();
        String[] fileNames = {
            "工作报告.pdf", "简历.docx", "财务报表.xlsx", "产品说明书.pdf",
            "合同文件.pdf", "项目计划.pptx", "用户手册.pdf", "设计图纸.pdf"
        };
        String[] fileTypes = {"pdf", "document", "spreadsheet", "presentation"};
        String[] userNames = {"张三", "李四", "王五", "赵六", "钱七", "孙八"};

        for (int i = 0; i < 20; i++) {
            PrintOrder order = new PrintOrder();
            
            // 基本信息
            order.setOrderNo(generateOrderNo(i));
            order.setUserId((long) (random.nextInt(6) + 1));
            order.setUserName(userNames[random.nextInt(userNames.length)]);
            order.setFileName(fileNames[random.nextInt(fileNames.length)]);
            order.setFileType(fileTypes[random.nextInt(fileTypes.length)]);
            
            // 打印配置
            order.setCopies(random.nextInt(5) + 1);
            order.setActualPages(random.nextInt(20) + 1);
            order.setIsColor(random.nextBoolean());
            order.setIsDoubleSide(random.nextBoolean());
            order.setPaperSize("A4");
            
            // 价格计算
            BigDecimal unitPrice = order.getIsColor() ? new BigDecimal("0.5") : new BigDecimal("0.1");
            BigDecimal amount = unitPrice.multiply(new BigDecimal(order.getActualPages()))
                                        .multiply(new BigDecimal(order.getCopies()));
            if (order.getIsDoubleSide()) {
                amount = amount.multiply(new BigDecimal("0.8"));
            }
            order.setAmount(amount);
            
            // 验证码
            order.setVerifyCode(generateVerifyCode(i));
            
            // 状态和时间
            int status = random.nextInt(6); // 0-5
            order.setStatus(status);
            
            LocalDateTime createTime = LocalDateTime.now().minusDays(random.nextInt(7))
                                                         .minusHours(random.nextInt(24));
            order.setCreateTime(createTime);
            order.setUpdateTime(createTime);
            
            if (status >= 1) { // 已支付
                order.setPayTime(createTime.plusMinutes(random.nextInt(30)));
            }
            
            if (status == 3) { // 已完成
                order.setFinishTime(createTime.plusHours(random.nextInt(48)));
            }
            
            // 备注
            if (random.nextBoolean()) {
                String[] remarks = {
                    "请在下午3点前完成", "加急处理", "双面打印请注意对齐",
                    "彩色打印质量要好", "需要装订", "请保密处理"
                };
                order.setRemark(remarks[random.nextInt(remarks.length)]);
            }
            
            orderRepository.save(order);
        }
    }

    private String generateOrderNo(int index) {
        return String.format("PC20240101%04d%04d", 
                            (int)(System.currentTimeMillis() % 10000), index);
    }

    private String generateVerifyCode(int index) {
        return String.format("%06d", 100000 + index);
    }
}
