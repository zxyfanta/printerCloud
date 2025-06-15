package com.printercloud.admin.view.swing;

import com.printercloud.admin.service.AuthService;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 登录窗口
 */
public class LoginFrame extends JFrame {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginFrame.class);
    
    private final SwingApplication app;
    private final AuthService authService;
    
    // UI组件
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberCheckBox;
    private JButton loginButton;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    
    public LoginFrame(SwingApplication app, AuthService authService) {
        this.app = app;
        this.authService = authService;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupWindow();
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 用户名输入框
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        usernameField.putClientProperty("JTextField.placeholderText", "请输入管理员用户名");
        
        // 密码输入框
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        passwordField.putClientProperty("JTextField.placeholderText", "请输入密码");
        
        // 记住我复选框
        rememberCheckBox = new JCheckBox("记住登录状态");
        rememberCheckBox.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        
        // 登录按钮
        loginButton = new JButton("登录");
        loginButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.putClientProperty("JButton.buttonType", "roundRect");
        
        // 状态标签
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 进度条
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new MigLayout("fill, insets 40", "[center]", "[]20[]30[]15[]15[]20[]10[]"));
        
        // 标题
        JLabel titleLabel = new JLabel("云打印管理系统");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, "wrap");
        
        // 副标题
        JLabel subtitleLabel = new JLabel("管理员登录");
        subtitleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setForeground(Color.GRAY);
        add(subtitleLabel, "wrap");
        
        // 表单面板
        JPanel formPanel = new JPanel(new MigLayout("insets 0", "[]10[]", "[]10[]"));
        formPanel.setBorder(BorderFactory.createTitledBorder(""));
        
        // 用户名
        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        formPanel.add(usernameLabel, "right");
        formPanel.add(usernameField, "growx, wrap");
        
        // 密码
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        formPanel.add(passwordLabel, "right");
        formPanel.add(passwordField, "growx, wrap");
        
        add(formPanel, "growx, wrap");
        
        // 记住我
        add(rememberCheckBox, "center, wrap");
        
        // 登录按钮
        add(loginButton, "center, wrap");
        
        // 进度条
        add(progressBar, "growx, wrap");
        
        // 状态标签
        add(statusLabel, "growx, wrap");
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 登录按钮点击事件
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        // 回车键登录
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
        
        // 窗口关闭事件
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                app.exit();
            }
        });
    }
    
    /**
     * 设置窗口属性
     */
    private void setupWindow() {
        setTitle("云打印管理系统 - 登录");
        setSize(450, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // 设置图标
        try {
            // 可以在这里设置应用程序图标
            // setIconImage(ImageIO.read(getClass().getResourceAsStream("/icons/app-icon.png")));
        } catch (Exception e) {
            logger.debug("设置应用程序图标失败", e);
        }
    }
    
    /**
     * 执行登录
     */
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // 验证输入
        if (username.isEmpty()) {
            showStatus("请输入用户名", Color.RED);
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showStatus("请输入密码", Color.RED);
            passwordField.requestFocus();
            return;
        }
        
        // 开始登录
        setLoginInProgress(true);
        showStatus("正在登录...", Color.BLUE);
        
        // 在后台线程执行登录
        SwingWorker<Boolean, Void> loginWorker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return authService.login(username, password);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    
                    if (success) {
                        showStatus("登录成功！", Color.GREEN);
                        
                        // 延迟跳转到主界面
                        Timer timer = new Timer(500, e -> {
                            app.showMainFrame();
                        });
                        timer.setRepeats(false);
                        timer.start();
                        
                    } else {
                        showStatus("用户名或密码错误", Color.RED);
                        passwordField.selectAll();
                        passwordField.requestFocus();
                    }
                    
                } catch (Exception e) {
                    logger.error("登录失败", e);
                    showStatus("登录失败: " + e.getMessage(), Color.RED);
                } finally {
                    setLoginInProgress(false);
                }
            }
        };
        
        loginWorker.execute();
    }
    
    /**
     * 设置登录进行中状态
     */
    private void setLoginInProgress(boolean inProgress) {
        loginButton.setEnabled(!inProgress);
        usernameField.setEnabled(!inProgress);
        passwordField.setEnabled(!inProgress);
        rememberCheckBox.setEnabled(!inProgress);
        progressBar.setVisible(inProgress);
        
        if (inProgress) {
            loginButton.setText("登录中...");
        } else {
            loginButton.setText("登录");
        }
    }
    
    /**
     * 显示状态信息
     */
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
    
    /**
     * 重置表单
     */
    public void resetForm() {
        usernameField.setText("");
        passwordField.setText("");
        rememberCheckBox.setSelected(false);
        showStatus(" ", Color.BLACK);
        setLoginInProgress(false);
        usernameField.requestFocus();
    }
}
