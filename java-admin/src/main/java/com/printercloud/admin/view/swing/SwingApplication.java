package com.printercloud.admin.view.swing;

import com.formdev.flatlaf.FlatLightLaf;
import com.printercloud.admin.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

/**
 * Swing应用程序主类
 * 负责初始化Swing界面和主题
 */
@Component
public class SwingApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(SwingApplication.class);
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private LoginFrame loginFrame;
    private MainFrame mainFrame;
    
    /**
     * 启动Swing应用程序
     */
    public void start() {
        logger.info("启动Swing应用程序...");
        
        // 设置系统外观
        setupLookAndFeel();
        
        // 设置系统属性
        setupSystemProperties();
        
        // 在EDT中创建和显示GUI
        SwingUtilities.invokeLater(() -> {
            try {
                createAndShowGUI();
            } catch (Exception e) {
                logger.error("创建GUI失败", e);
                showErrorDialog("启动失败", "无法创建用户界面: " + e.getMessage());
            }
        });
    }
    
    /**
     * 设置外观和感觉
     */
    private void setupLookAndFeel() {
        try {
            // 使用FlatLaf现代化主题
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // 设置字体
            Font defaultFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
            UIManager.put("defaultFont", defaultFont);
            
            // 自定义颜色
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            
            logger.info("FlatLaf主题设置成功");
            
        } catch (Exception e) {
            logger.warn("设置FlatLaf主题失败，使用系统默认主题", e);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                logger.error("设置系统主题也失败", ex);
            }
        }
    }
    
    /**
     * 设置系统属性
     */
    private void setupSystemProperties() {
        // 启用抗锯齿
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // 设置应用程序名称
        System.setProperty("apple.awt.application.name", "云打印管理系统");
        
        // macOS特定设置
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "云打印管理系统");
        }
    }
    
    /**
     * 创建和显示GUI
     */
    private void createAndShowGUI() {
        logger.info("创建主界面...");
        
        // 检查是否已登录
        if (authService.isLoggedIn()) {
            showMainFrame();
        } else {
            showLoginFrame();
        }
    }
    
    /**
     * 显示登录窗口
     */
    public void showLoginFrame() {
        SwingUtilities.invokeLater(() -> {
            if (loginFrame == null) {
                loginFrame = new LoginFrame(this, authService);
            }
            
            // 隐藏主窗口
            if (mainFrame != null) {
                mainFrame.setVisible(false);
            }
            
            loginFrame.setVisible(true);
            loginFrame.toFront();
            
            logger.info("显示登录窗口");
        });
    }
    
    /**
     * 显示主窗口
     */
    public void showMainFrame() {
        SwingUtilities.invokeLater(() -> {
            if (mainFrame == null) {
                mainFrame = new MainFrame(this, authService);
            }
            
            // 隐藏登录窗口
            if (loginFrame != null) {
                loginFrame.setVisible(false);
            }
            
            mainFrame.setVisible(true);
            mainFrame.toFront();
            
            logger.info("显示主窗口");
        });
    }
    
    /**
     * 退出应用程序
     */
    public void exit() {
        logger.info("退出应用程序");
        
        // 执行清理操作
        if (authService.isLoggedIn()) {
            authService.logout();
        }
        
        // 关闭所有窗口
        if (loginFrame != null) {
            loginFrame.dispose();
        }
        if (mainFrame != null) {
            mainFrame.dispose();
        }
        
        System.exit(0);
    }
    
    /**
     * 显示错误对话框
     */
    public void showErrorDialog(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
    
    /**
     * 显示信息对话框
     */
    public void showInfoDialog(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }
    
    /**
     * 显示确认对话框
     */
    public boolean showConfirmDialog(String title, String message) {
        return JOptionPane.showConfirmDialog(
            null,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }
    
    /**
     * 获取Spring应用程序上下文
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
