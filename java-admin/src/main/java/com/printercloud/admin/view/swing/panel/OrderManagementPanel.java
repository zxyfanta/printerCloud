package com.printercloud.admin.view.swing.panel;

import com.printercloud.admin.model.Order;
import com.printercloud.admin.service.OrderService;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 订单管理面板
 */
public class OrderManagementPanel extends JPanel {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderManagementPanel.class);
    
    @Autowired
    private OrderService orderService;
    
    // UI组件
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JButton refreshButton;
    private JButton viewButton;
    private JButton completeButton;
    private JButton cancelButton;
    
    // 分页组件
    private JLabel pageLabel;
    private JButton prevButton;
    private JButton nextButton;
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalPages = 1;
    
    public OrderManagementPanel() {
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
        searchField.putClientProperty("JTextField.placeholderText", "搜索订单号、文件名...");
        
        // 状态筛选
        statusFilter = new JComboBox<>(new String[]{
            "全部状态", "待支付", "已支付", "打印中", "已完成", "已取消"
        });
        
        // 创建表格
        createTable();
        
        // 统计标签
        totalLabel = new JLabel("总计: 0 条记录");
        totalLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        
        // 按钮
        refreshButton = new JButton("刷新");
        viewButton = new JButton("查看详情");
        completeButton = new JButton("完成订单");
        cancelButton = new JButton("取消订单");
        
        // 分页组件
        pageLabel = new JLabel("第 1 页，共 1 页");
        prevButton = new JButton("上一页");
        nextButton = new JButton("下一页");
        
        // 设置按钮状态
        updateButtonStates();
    }
    
    /**
     * 创建表格
     */
    private void createTable() {
        String[] columnNames = {
            "订单号", "文件名", "用户", "金额", "状态", "创建时间", "操作"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // 只有操作列可编辑
            }
        };
        
        orderTable = new JTable(tableModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.setRowHeight(30);
        orderTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        orderTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        
        // 设置列宽
        orderTable.getColumnModel().getColumn(0).setPreferredWidth(120); // 订单号
        orderTable.getColumnModel().getColumn(1).setPreferredWidth(200); // 文件名
        orderTable.getColumnModel().getColumn(2).setPreferredWidth(100); // 用户
        orderTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // 金额
        orderTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 状态
        orderTable.getColumnModel().getColumn(5).setPreferredWidth(150); // 创建时间
        orderTable.getColumnModel().getColumn(6).setPreferredWidth(120); // 操作
        
        // 设置状态列渲染器
        orderTable.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        
        // 设置操作列渲染器
        orderTable.getColumnModel().getColumn(6).setCellRenderer(new ActionCellRenderer());
        orderTable.getColumnModel().getColumn(6).setCellEditor(new ActionCellEditor());
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
        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("订单列表"));
        add(scrollPane, "grow, wrap");
        
        // 底部面板
        JPanel bottomPanel = new JPanel(new MigLayout("insets 0", "[]10[]10[]push[]10[]10[]", "[]"));
        
        bottomPanel.add(viewButton);
        bottomPanel.add(completeButton);
        bottomPanel.add(cancelButton);
        bottomPanel.add(pageLabel);
        bottomPanel.add(prevButton);
        bottomPanel.add(nextButton);
        
        add(bottomPanel, "growx");
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
        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // 操作按钮
        viewButton.addActionListener(e -> viewOrderDetails());
        completeButton.addActionListener(e -> completeOrder());
        cancelButton.addActionListener(e -> cancelOrder());
        
        // 分页按钮
        prevButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadData();
            }
        });
        
        nextButton.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadData();
            }
        });
    }
    
    /**
     * 加载数据
     */
    private void loadData() {
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                String search = searchField.getText().trim();
                String status = statusFilter.getSelectedItem().toString();
                Integer statusCode = "全部状态".equals(status) ? null : getStatusCode(status);
                
                return orderService.getOrders(currentPage, pageSize, search, statusCode, "createTime", "desc");
            }
            
            @Override
            protected void done() {
                try {
                    Map<String, Object> result = get();
                    updateTable(result);
                } catch (Exception e) {
                    logger.error("加载订单数据失败", e);
                    JOptionPane.showMessageDialog(
                        OrderManagementPanel.this,
                        "加载数据失败: " + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * 更新表格数据
     */
    private void updateTable(Map<String, Object> result) {
        // 清空现有数据
        tableModel.setRowCount(0);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> orders = (List<Map<String, Object>>) result.get("content");
        
        if (orders != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            
            for (Map<String, Object> order : orders) {
                Object[] row = {
                    order.get("orderNo"),
                    order.get("fileName"),
                    order.get("username"),
                    String.format("%.2f", ((Number) order.get("amount")).doubleValue()),
                    getStatusText(((Number) order.get("status")).intValue()),
                    sdf.format(new Date(((Number) order.get("createTime")).longValue())),
                    "操作"
                };
                tableModel.addRow(row);
            }
        }
        
        // 更新分页信息
        int totalElements = ((Number) result.get("totalElements")).intValue();
        totalPages = ((Number) result.get("totalPages")).intValue();
        
        totalLabel.setText(String.format("总计: %d 条记录", totalElements));
        pageLabel.setText(String.format("第 %d 页，共 %d 页", currentPage, totalPages));
        
        // 更新按钮状态
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
        updateButtonStates();
    }
    
    /**
     * 搜索
     */
    private void search() {
        currentPage = 1;
        loadData();
    }
    
    /**
     * 查看订单详情
     */
    private void viewOrderDetails() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            String orderNo = (String) tableModel.getValueAt(selectedRow, 0);
            showOrderDetailsDialog(orderNo);
        }
    }
    
    /**
     * 完成订单
     */
    private void completeOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            String orderNo = (String) tableModel.getValueAt(selectedRow, 0);
            
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要完成订单 " + orderNo + " 吗？",
                "确认完成",
                JOptionPane.YES_NO_OPTION
            );
            
            if (result == JOptionPane.YES_OPTION) {
                // 这里调用完成订单的服务
                JOptionPane.showMessageDialog(this, "订单已完成");
                loadData();
            }
        }
    }
    
    /**
     * 取消订单
     */
    private void cancelOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            String orderNo = (String) tableModel.getValueAt(selectedRow, 0);
            
            int result = JOptionPane.showConfirmDialog(
                this,
                "确定要取消订单 " + orderNo + " 吗？",
                "确认取消",
                JOptionPane.YES_NO_OPTION
            );
            
            if (result == JOptionPane.YES_OPTION) {
                // 这里调用取消订单的服务
                JOptionPane.showMessageDialog(this, "订单已取消");
                loadData();
            }
        }
    }
    
    /**
     * 显示订单详情对话框
     */
    private void showOrderDetailsDialog(String orderNo) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "订单详情", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setText("订单号: " + orderNo + "\n\n详细信息加载中...");
        
        dialog.add(new JScrollPane(textArea));
        dialog.setVisible(true);
    }
    
    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        boolean hasSelection = orderTable.getSelectedRow() >= 0;
        viewButton.setEnabled(hasSelection);
        completeButton.setEnabled(hasSelection);
        cancelButton.setEnabled(hasSelection);
    }
    
    /**
     * 获取状态代码
     */
    private Integer getStatusCode(String statusText) {
        switch (statusText) {
            case "待支付": return 0;
            case "已支付": return 1;
            case "打印中": return 2;
            case "已完成": return 3;
            case "已取消": return 4;
            default: return null;
        }
    }
    
    /**
     * 获取状态文本
     */
    private String getStatusText(int statusCode) {
        switch (statusCode) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "打印中";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
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
            switch (status) {
                case "待支付":
                    setForeground(Color.ORANGE);
                    break;
                case "已支付":
                    setForeground(Color.BLUE);
                    break;
                case "打印中":
                    setForeground(Color.MAGENTA);
                    break;
                case "已完成":
                    setForeground(Color.GREEN);
                    break;
                case "已取消":
                    setForeground(Color.RED);
                    break;
                default:
                    setForeground(Color.BLACK);
            }
            
            return this;
        }
    }
    
    /**
     * 操作单元格渲染器
     */
    private class ActionCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JButton button = new JButton("操作");
            button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            return button;
        }
    }
    
    /**
     * 操作单元格编辑器
     */
    private class ActionCellEditor extends DefaultCellEditor {
        private JButton button;
        
        public ActionCellEditor() {
            super(new JCheckBox());
            button = new JButton("操作");
            button.setOpaque(true);
            button.addActionListener(e -> {
                int row = orderTable.getSelectedRow();
                if (row >= 0) {
                    viewOrderDetails();
                }
                fireEditingStopped();
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "操作";
        }
    }
}
