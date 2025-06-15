package com.printercloud.admin.view.swing;

import com.printercloud.admin.service.AuthService;
import com.printercloud.admin.service.OrderService;
import com.printercloud.admin.view.swing.panel.*;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 主窗口
 */
public class MainFrame extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    
    private final SwingApplication app;
    private final AuthService authService;
    
    // UI组件
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JPanel sidePanel;
    private JPanel contentPanel;
    private JLabel statusLabel;
    private JPanel statusPanel;
    private CardLayout cardLayout;
    
    // 视图面板
    private final Map<String, JPanel> viewPanels = new HashMap<>();
    private String currentView = "dashboard";
    
    public MainFrame(SwingApplication app, AuthService authService) {
        this.app = app;
        this.authService = authService;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupWindow();
        
        // 默认显示仪表盘
        showView("dashboard");
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 创建菜单栏
        createMenuBar();
        
        // 创建工具栏
        createToolBar();
        
        // 创建侧边栏
        createSidePanel();
        
        // 创建内容面板
        createContentPanel();
        
        // 创建状态栏
        createStatusBar();
        
        // 创建视图面板
        createViewPanels();
    }
    
    /**
     * 创建菜单栏
     */
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // 文件菜单
        JMenu fileMenu = new JMenu("文件(F)");
        fileMenu.setMnemonic('F');
        
        JMenuItem refreshItem = new JMenuItem("刷新(R)");
        refreshItem.setMnemonic('R');
        refreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        refreshItem.addActionListener(e -> refreshCurrentView());
        
        JMenuItem exitItem = new JMenuItem("退出(X)");
        exitItem.setMnemonic('X');
        exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        exitItem.addActionListener(e -> exit());
        
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // 视图菜单
        JMenu viewMenu = new JMenu("视图(V)");
        viewMenu.setMnemonic('V');
        
        JMenuItem dashboardItem = new JMenuItem("仪表盘");
        dashboardItem.addActionListener(e -> showView("dashboard"));
        
        JMenuItem ordersItem = new JMenuItem("订单管理");
        ordersItem.addActionListener(e -> showView("orders"));
        
        JMenuItem filesItem = new JMenuItem("文件管理");
        filesItem.addActionListener(e -> showView("files"));
        
        JMenuItem usersItem = new JMenuItem("用户管理");
        usersItem.addActionListener(e -> showView("users"));
        
        JMenuItem printersItem = new JMenuItem("打印机管理");
        printersItem.addActionListener(e -> showView("printers"));
        
        viewMenu.add(dashboardItem);
        viewMenu.add(ordersItem);
        viewMenu.add(filesItem);
        viewMenu.add(usersItem);
        viewMenu.add(printersItem);
        
        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助(H)");
        helpMenu.setMnemonic('H');
        
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> showAboutDialog());
        
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);
    }
    
    /**
     * 创建工具栏
     */
    private void createToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(222, 226, 230)));
        toolBar.setPreferredSize(new Dimension(0, 50));

        // 刷新按钮
        JButton refreshButton = createToolBarButton("🔄 刷新", "刷新当前视图 (F5)");
        refreshButton.addActionListener(e -> refreshCurrentView());

        // 用户信息面板
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);

        // 用户头像和信息
        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 20));

        JLabel userLabel = new JLabel();
        if (authService.getCurrentUser() != null) {
            userLabel.setText("欢迎，" + authService.getCurrentUser().getUsername());
        } else {
            userLabel.setText("欢迎，管理员");
        }
        userLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        userLabel.setForeground(new Color(52, 58, 64));

        // 登出按钮
        JButton logoutButton = createToolBarButton("登出", "退出登录");
        logoutButton.addActionListener(e -> logout());

        userPanel.add(avatarLabel);
        userPanel.add(userLabel);
        userPanel.add(Box.createHorizontalStrut(10));
        userPanel.add(logoutButton);

        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(refreshButton);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(userPanel);
        toolBar.add(Box.createHorizontalStrut(10));
    }

    /**
     * 创建工具栏按钮
     */
    private JButton createToolBarButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(80, 30));

        // 设置按钮样式
        button.setBackground(new Color(0, 123, 255));
        button.setForeground(Color.WHITE);
        button.setOpaque(true);

        // 鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(0, 86, 179));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(0, 123, 255));
            }
        });

        return button;
    }
    
    /**
     * 创建侧边栏
     */
    private void createSidePanel() {
        sidePanel = new JPanel(new MigLayout("fill, insets 15", "[grow]", "[]5[]5[]5[]5[]"));
        sidePanel.setPreferredSize(new Dimension(220, 0));
        sidePanel.setBackground(new Color(248, 249, 250));
        sidePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));

        // 添加标题
        JLabel titleLabel = new JLabel("🖨️ 云打印管理");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        titleLabel.setForeground(new Color(52, 58, 64));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        sidePanel.add(titleLabel, "growx, wrap, gapbottom 20");

        // 创建导航按钮
        JButton dashboardBtn = createNavButton("📊 仪表盘", "dashboard");
        JButton ordersBtn = createNavButton("📋 订单管理", "orders");
        JButton filesBtn = createNavButton("📁 文件管理", "files");
        JButton usersBtn = createNavButton("👥 用户管理", "users");
        JButton printersBtn = createNavButton("🖨️ 打印机管理", "printers");

        // 设置默认选中状态
        dashboardBtn.setBackground(new Color(0, 123, 255));
        dashboardBtn.setForeground(Color.WHITE);

        sidePanel.add(dashboardBtn, "growx, wrap");
        sidePanel.add(ordersBtn, "growx, wrap");
        sidePanel.add(filesBtn, "growx, wrap");
        sidePanel.add(usersBtn, "growx, wrap");
        sidePanel.add(printersBtn, "growx, wrap");
    }
    
    /**
     * 创建导航按钮
     */
    private JButton createNavButton(String text, String viewName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setPreferredSize(new Dimension(180, 45));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        // 设置按钮样式
        button.setBackground(new Color(248, 249, 250));
        button.setForeground(new Color(52, 58, 64));

        // 鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!viewName.equals(currentView)) {
                    button.setBackground(new Color(233, 236, 239));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!viewName.equals(currentView)) {
                    button.setBackground(new Color(248, 249, 250));
                }
            }
        });

        button.addActionListener(e -> {
            // 重置所有按钮样式
            resetNavButtonStyles();
            // 设置当前按钮为选中状态
            button.setBackground(new Color(0, 123, 255));
            button.setForeground(Color.WHITE);
            showView(viewName);
        });

        return button;
    }

    /**
     * 重置导航按钮样式
     */
    private void resetNavButtonStyles() {
        for (Component comp : sidePanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                btn.setBackground(new Color(248, 249, 250));
                btn.setForeground(new Color(52, 58, 64));
            }
        }
    }
    
    /**
     * 创建内容面板
     */
    private void createContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(BorderFactory.createTitledBorder(""));
    }
    
    /**
     * 创建状态栏
     */
    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(248, 249, 250));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        statusPanel.setPreferredSize(new Dimension(0, 30));

        statusLabel = new JLabel("✅ 系统就绪");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(52, 58, 64));

        // 添加时间显示
        JLabel timeLabel = new JLabel();
        timeLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        timeLabel.setForeground(new Color(108, 117, 125));

        // 更新时间的定时器
        Timer timer = new Timer(1000, e -> {
            timeLabel.setText(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ));
        });
        timer.start();

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(timeLabel, BorderLayout.EAST);

        // 保存状态面板的引用，用于后续在setupLayout中添加
        this.statusPanel = statusPanel;
    }
    
    /**
     * 创建视图面板
     */
    private void createViewPanels() {
        // 仪表盘
        viewPanels.put("dashboard", new DashboardPanel());

        // 订单管理 - 需要传入服务依赖
        OrderManagementPanel orderPanel = new OrderManagementPanel();
        // 通过Spring上下文获取OrderService
        try {
            orderPanel.setOrderService(OrderService);
        } catch (Exception e) {
            logger.warn("无法获取OrderService，订单管理功能可能受限", e);
        }
        viewPanels.put("orders", orderPanel);

        // 文件管理
        viewPanels.put("files", new FileManagementPanel());

        // 用户管理
        viewPanels.put("users", new UserManagementPanel());

        // 打印机管理
        viewPanels.put("printers", new PrinterManagementPanel());

        // 添加到内容面板
        for (Map.Entry<String, JPanel> entry : viewPanels.entrySet()) {
            contentPanel.add(entry.getValue(), entry.getKey());
        }
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());

        // 添加组件
        setJMenuBar(menuBar);
        add(toolBar, BorderLayout.NORTH);
        add(sidePanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                exit();
            }
        });
    }
    
    /**
     * 设置窗口属性
     */
    private void setupWindow() {
        setTitle("云打印管理系统");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    /**
     * 显示指定视图
     */
    public void showView(String viewName) {
        if (viewPanels.containsKey(viewName)) {
            cardLayout.show(contentPanel, viewName);
            currentView = viewName;
            updateStatus("已切换到" + getViewDisplayName(viewName));
            logger.info("切换到视图: {}", viewName);
        }
    }
    
    /**
     * 获取视图显示名称
     */
    private String getViewDisplayName(String viewName) {
        switch (viewName) {
            case "dashboard": return "仪表盘";
            case "orders": return "订单管理";
            case "files": return "文件管理";
            case "users": return "用户管理";
            case "printers": return "打印机管理";
            default: return viewName;
        }
    }
    
    /**
     * 刷新当前视图
     */
    private void refreshCurrentView() {
        updateStatus("🔄 正在刷新...");

        SwingUtilities.invokeLater(() -> {
            try {
                // 获取当前视图面板
                JPanel currentPanel = viewPanels.get(currentView);
                if (currentPanel != null) {
                    // 如果面板有refresh方法，调用它
                    try {
                        java.lang.reflect.Method refreshMethod = currentPanel.getClass().getMethod("refresh");
                        refreshMethod.invoke(currentPanel);
                        logger.info("已刷新视图: {}", currentView);
                    } catch (Exception e) {
                        logger.debug("视图 {} 没有refresh方法，跳过刷新", currentView);
                    }
                }

                updateStatus("✅ 刷新完成 - " + getViewDisplayName(currentView));

                // 3秒后恢复默认状态
                Timer timer = new Timer(3000, e -> updateStatus("✅ 系统就绪"));
                timer.setRepeats(false);
                timer.start();

            } catch (Exception e) {
                logger.error("刷新视图失败", e);
                updateStatus("❌ 刷新失败");
            }
        });
    }
    
    /**
     * 登出
     */
    private void logout() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "确定要退出登录吗？",
            "确认登出",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            authService.logout();
            app.showLoginFrame();
        }
    }
    
    /**
     * 显示关于对话框
     */
    private void showAboutDialog() {
        String message = "云打印管理系统\n\n" +
                        "版本: 1.0.0\n" +
                        "基于 Java Swing 开发\n\n" +
                        "© 2024 云打印科技";
        
        JOptionPane.showMessageDialog(
            this,
            message,
            "关于",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * 更新状态栏
     */
    public void updateStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * 退出应用程序
     */
    public void exit() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "确定要退出云打印管理系统吗？",
            "确认退出",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            logger.info("用户确认退出应用程序");
            System.exit(0);
        }
    }
}
