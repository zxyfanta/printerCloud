package com.printercloud.admin.view.swing.panel;

import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 仪表盘面板
 */
public class DashboardPanel extends JPanel {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardPanel.class);
    
    // 统计卡片
    private JLabel totalOrdersLabel;
    private JLabel todayOrdersLabel;
    private JLabel totalUsersLabel;
    private JLabel onlinePrintersLabel;
    
    // 图表
    private ChartPanel orderTrendChart;
    private ChartPanel statusPieChart;
    
    public DashboardPanel() {
        initializeComponents();
        setupLayout();
        loadData();
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 创建统计卡片
        createStatCards();
        
        // 创建图表
        createCharts();
    }
    
    /**
     * 创建统计卡片
     */
    private void createStatCards() {
        totalOrdersLabel = new JLabel("0");
        todayOrdersLabel = new JLabel("0");
        totalUsersLabel = new JLabel("0");
        onlinePrintersLabel = new JLabel("0");
        
        // 设置字体
        Font numberFont = new Font("Microsoft YaHei", Font.BOLD, 24);
        totalOrdersLabel.setFont(numberFont);
        todayOrdersLabel.setFont(numberFont);
        totalUsersLabel.setFont(numberFont);
        onlinePrintersLabel.setFont(numberFont);
        
        // 设置颜色
        totalOrdersLabel.setForeground(new Color(52, 152, 219));
        todayOrdersLabel.setForeground(new Color(46, 204, 113));
        totalUsersLabel.setForeground(new Color(155, 89, 182));
        onlinePrintersLabel.setForeground(new Color(230, 126, 34));
    }
    
    /**
     * 创建图表
     */
    private void createCharts() {
        // 订单趋势图
        createOrderTrendChart();
        
        // 状态饼图
        createStatusPieChart();
    }
    
    /**
     * 创建订单趋势图
     */
    private void createOrderTrendChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // 模拟数据
        dataset.addValue(15, "订单数", "周一");
        dataset.addValue(23, "订单数", "周二");
        dataset.addValue(18, "订单数", "周三");
        dataset.addValue(32, "订单数", "周四");
        dataset.addValue(28, "订单数", "周五");
        dataset.addValue(45, "订单数", "周六");
        dataset.addValue(38, "订单数", "周日");
        
        JFreeChart chart = ChartFactory.createLineChart(
            "本周订单趋势",
            "日期",
            "订单数量",
            dataset
        );
        
        // 设置图表样式
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        
        orderTrendChart = new ChartPanel(chart);
        orderTrendChart.setPreferredSize(new Dimension(400, 250));
    }
    
    /**
     * 创建状态饼图
     */
    private void createStatusPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        // 模拟数据
        dataset.setValue("已完成", 65);
        dataset.setValue("进行中", 20);
        dataset.setValue("待支付", 10);
        dataset.setValue("已取消", 5);
        
        JFreeChart chart = ChartFactory.createPieChart(
            "订单状态分布",
            dataset,
            true,
            true,
            false
        );
        
        // 设置图表样式
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        
        statusPieChart = new ChartPanel(chart);
        statusPieChart.setPreferredSize(new Dimension(400, 250));
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new MigLayout("fill, insets 20", "[grow][grow]", "[]20[]"));
        
        // 顶部统计卡片区域
        JPanel statsPanel = new JPanel(new MigLayout("fill", "[grow][grow][grow][grow]", "[]"));
        
        // 创建统计卡片
        JPanel totalOrdersCard = createStatCard("总订单数", totalOrdersLabel, new Color(52, 152, 219));
        JPanel todayOrdersCard = createStatCard("今日订单", todayOrdersLabel, new Color(46, 204, 113));
        JPanel totalUsersCard = createStatCard("总用户数", totalUsersLabel, new Color(155, 89, 182));
        JPanel onlinePrintersCard = createStatCard("在线打印机", onlinePrintersLabel, new Color(230, 126, 34));
        
        statsPanel.add(totalOrdersCard, "grow");
        statsPanel.add(todayOrdersCard, "grow");
        statsPanel.add(totalUsersCard, "grow");
        statsPanel.add(onlinePrintersCard, "grow");
        
        add(statsPanel, "span 2, growx, wrap");
        
        // 图表区域
        JPanel chartPanel1 = new JPanel(new BorderLayout());
        chartPanel1.setBorder(new TitledBorder("订单趋势"));
        chartPanel1.add(orderTrendChart, BorderLayout.CENTER);
        
        JPanel chartPanel2 = new JPanel(new BorderLayout());
        chartPanel2.setBorder(new TitledBorder("状态分布"));
        chartPanel2.add(statusPieChart, BorderLayout.CENTER);
        
        add(chartPanel1, "grow");
        add(chartPanel2, "grow");
        
        // 底部信息面板
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, "span 2, growx");
    }
    
    /**
     * 创建统计卡片
     */
    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new MigLayout("fill, insets 15", "[center]", "[]10[]"));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);
        
        // 标题
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 数值
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(titleLabel, "wrap");
        card.add(valueLabel, "grow");
        
        return card;
    }
    
    /**
     * 创建信息面板
     */
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[]"));
        panel.setBorder(new TitledBorder("系统信息"));
        
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        infoArea.setBackground(getBackground());
        
        // 系统信息
        StringBuilder info = new StringBuilder();
        info.append("系统状态: 正常运行\n");
        info.append("当前时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        info.append("Java版本: ").append(System.getProperty("java.version")).append("\n");
        info.append("操作系统: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append("\n");
        info.append("内存使用: ").append(getMemoryInfo()).append("\n");
        
        infoArea.setText(info.toString());
        
        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setPreferredSize(new Dimension(0, 100));
        
        panel.add(scrollPane, "grow");
        
        return panel;
    }
    
    /**
     * 获取内存信息
     */
    private String getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return String.format("%.1f MB / %.1f MB", 
            usedMemory / 1024.0 / 1024.0, 
            totalMemory / 1024.0 / 1024.0);
    }
    
    /**
     * 加载数据
     */
    private void loadData() {
        // 模拟加载数据
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // 模拟网络延迟
                Thread.sleep(1000);
                return null;
            }
            
            @Override
            protected void done() {
                // 更新统计数据
                totalOrdersLabel.setText("1,234");
                todayOrdersLabel.setText("56");
                totalUsersLabel.setText("789");
                onlinePrintersLabel.setText("12");
                
                logger.info("仪表盘数据加载完成");
            }
        };
        
        worker.execute();
    }
    
    /**
     * 刷新数据
     */
    public void refresh() {
        logger.info("刷新仪表盘数据");
        loadData();
    }
}
