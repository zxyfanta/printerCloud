package com.printercloud.admin.view.swing.panel;

import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * 用户管理面板
 */
public class UserManagementPanel extends JPanel {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementPanel.class);
    
    // UI组件
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JButton refreshButton;
    private JButton viewButton;
    private JButton editButton;
    private JButton resetPasswordButton;
    private JButton toggleStatusButton;
    
    public UserManagementPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadData();
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 搜索框
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "搜索用户名、昵称...");
        
        // 状态筛选
        statusFilter = new JComboBox<>(new String[]{
            "全部状态", "正常", "禁用"
        });
        
        // 创建表格
        createTable();
        
        // 统计标签
        totalLabel = new JLabel("总计: 0 个用户");
        totalLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        
        // 按钮
        refreshButton = new JButton("刷新");
        viewButton = new JButton("查看详情");
        editButton = new JButton("编辑用户");
        resetPasswordButton = new JButton("重置密码");
        toggleStatusButton = new JButton("切换状态");
        
        // 设置按钮状态
        updateButtonStates();
    }
    
    /**
     * 创建表格
     */
    private void createTable() {
        String[] columnNames = {
            "用户名", "昵称", "手机号", "邮箱", "状态", "注册时间", "最后登录"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(25);
        userTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        userTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        
        // 设置列宽
        userTable.getColumnModel().getColumn(0).setPreferredWidth(120); // 用户名
        userTable.getColumnModel().getColumn(1).setPreferredWidth(120); // 昵称
        userTable.getColumnModel().getColumn(2).setPreferredWidth(120); // 手机号
        userTable.getColumnModel().getColumn(3).setPreferredWidth(180); // 邮箱
        userTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 状态
        userTable.getColumnModel().getColumn(5).setPreferredWidth(150); // 注册时间
        userTable.getColumnModel().getColumn(6).setPreferredWidth(150); // 最后登录
        
        // 设置状态列渲染器
        userTable.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new MigLayout("fill, insets 10", "[grow]", "[]10[]10[grow]10[]"));
        
        // 顶部工具栏
        JPanel toolPanel = new JPanel(new MigLayout("insets 0", "[]10[]10[]push[]", "[]"));
        
        toolPanel.add(new JLabel("搜索:"));
        toolPanel.add(searchField);
        toolPanel.add(statusFilter);
        toolPanel.add(refreshButton);
        
        add(toolPanel, "growx, wrap");
        
        // 统计信息
        add(totalLabel, "wrap");
        
        // 表格
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("用户列表"));
        add(scrollPane, "grow, wrap");
        
        // 底部按钮
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[]10[]10[]10[]", "[]"));
        
        buttonPanel.add(viewButton);
        buttonPanel.add(editButton);
        buttonPanel.add(resetPasswordButton);
        buttonPanel.add(toggleStatusButton);
        
        add(buttonPanel, "left");
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 刷新按钮
        refreshButton.addActionListener(e -> loadData());
        
        // 搜索框
        searchField.addActionListener(e -> search());
        
        // 状态筛选
        statusFilter.addActionListener(e -> search());
        
        // 表格选择事件
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // 操作按钮
        viewButton.addActionListener(e -> viewUserDetails());
        editButton.addActionListener(e -> editUser());
        resetPasswordButton.addActionListener(e -> resetPassword());
        toggleStatusButton.addActionListener(e -> toggleUserStatus());
    }
    
    /**
     * 加载数据
     */
    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // 模拟网络延迟
                Thread.sleep(500);
                return null;
            }
            
            @Override
            protected void done() {
                // 模拟数据
                tableModel.setRowCount(0);
                
                Object[][] sampleData = {
                    {"zhangsan", "张三", "13800138001", "zhangsan@example.com", "正常", "2024-01-10 09:30", "2024-01-15 14:20"},
                    {"lisi", "李四", "13800138002", "lisi@example.com", "正常", "2024-01-08 16:45", "2024-01-15 10:15"},
                    {"wangwu", "王五", "13800138003", "wangwu@example.com", "禁用", "2024-01-05 11:20", "2024-01-12 08:30"},
                    {"zhaoliu", "赵六", "13800138004", "zhaoliu@example.com", "正常", "2024-01-03 14:10", "2024-01-14 19:45"},
                    {"qianqi", "钱七", "13800138005", "qianqi@example.com", "正常", "2024-01-01 10:00", "2024-01-15 12:30"}
                };
                
                for (Object[] row : sampleData) {
                    tableModel.addRow(row);
                }
                
                totalLabel.setText("总计: " + sampleData.length + " 个用户");
                updateButtonStates();
                
                logger.info("用户列表数据加载完成");
            }
        };
        
        worker.execute();
    }
    
    /**
     * 搜索
     */
    private void search() {
        logger.info("搜索用户: {}, 状态: {}", searchField.getText(), statusFilter.getSelectedItem());
        loadData(); // 重新加载数据
    }
    
    /**
     * 查看用户详情
     */
    private void viewUserDetails() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            String username = (String) tableModel.getValueAt(selectedRow, 0);
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "用户详情", true);
            dialog.setSize(500, 400);
            dialog.setLocationRelativeTo(this);
            
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            
            StringBuilder details = new StringBuilder();
            details.append("用户名: ").append(tableModel.getValueAt(selectedRow, 0)).append("\n");
            details.append("昵称: ").append(tableModel.getValueAt(selectedRow, 1)).append("\n");
            details.append("手机号: ").append(tableModel.getValueAt(selectedRow, 2)).append("\n");
            details.append("邮箱: ").append(tableModel.getValueAt(selectedRow, 3)).append("\n");
            details.append("状态: ").append(tableModel.getValueAt(selectedRow, 4)).append("\n");
            details.append("注册时间: ").append(tableModel.getValueAt(selectedRow, 5)).append("\n");
            details.append("最后登录: ").append(tableModel.getValueAt(selectedRow, 6)).append("\n");
            
            textArea.setText(details.toString());
            
            dialog.add(new JScrollPane(textArea));
            dialog.setVisible(true);
            
            logger.info("查看用户详情: {}", username);
        }
    }
    
    /**
     * 编辑用户
     */
    private void editUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            String username = (String) tableModel.getValueAt(selectedRow, 0);
            
            JOptionPane.showMessageDialog(this, "编辑用户功能开发中: " + username);
            logger.info("编辑用户: {}", username);
        }
    }
    
    /**
     * 重置密码
     */
    private void resetPassword() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            String username = (String) tableModel.getValueAt(selectedRow, 0);
            
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要重置用户 \"" + username + "\" 的密码吗？\n新密码将发送到用户邮箱。",
                "确认重置密码",
                JOptionPane.YES_NO_OPTION
            );
            
            if (result == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "密码重置成功，新密码已发送到用户邮箱");
                logger.info("重置用户密码: {}", username);
            }
        }
    }
    
    /**
     * 切换用户状态
     */
    private void toggleUserStatus() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            String username = (String) tableModel.getValueAt(selectedRow, 0);
            String currentStatus = (String) tableModel.getValueAt(selectedRow, 4);
            String newStatus = "正常".equals(currentStatus) ? "禁用" : "正常";
            
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要将用户 \"" + username + "\" 的状态从 \"" + currentStatus + "\" 改为 \"" + newStatus + "\" 吗？",
                "确认状态变更",
                JOptionPane.YES_NO_OPTION
            );
            
            if (result == JOptionPane.YES_OPTION) {
                tableModel.setValueAt(newStatus, selectedRow, 4);
                JOptionPane.showMessageDialog(this, "用户状态已更新");
                logger.info("切换用户状态: {} -> {}", username, newStatus);
            }
        }
    }
    
    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        boolean hasSelection = userTable.getSelectedRow() >= 0;
        viewButton.setEnabled(hasSelection);
        editButton.setEnabled(hasSelection);
        resetPasswordButton.setEnabled(hasSelection);
        toggleStatusButton.setEnabled(hasSelection);
    }
    
    /**
     * 状态单元格渲染器
     */
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String status = (String) value;
            if ("正常".equals(status)) {
                setForeground(Color.GREEN);
            } else if ("禁用".equals(status)) {
                setForeground(Color.RED);
            } else {
                setForeground(Color.BLACK);
            }
            
            return this;
        }
    }
    
    /**
     * 刷新数据
     */
    public void refresh() {
        logger.info("刷新用户管理数据");
        loadData();
    }
}
