import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;


        // 读取文本文件
public class TextAnalyzerVisualizer extends JFrame {
    private static HashMap<String, HashMap<String, Integer>> graph = new HashMap<>();
    private JPanel mainPanel, topPanel, centerPanel;
    private static boolean stop = false;
    private static boolean fileSelected = false; // 添加一个标志来表示是否已选择文件
    private static String str="";
    private static Map<String, Map<String, Integer>> map = new HashMap<>();
    public TextAnalyzerVisualizer() {
        setTitle("Text Analyzer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());
        topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel = new JPanel();

        JButton selectFileButton = new JButton("选择文件");
        selectFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectFile();
            }
        });
        topPanel.add(selectFileButton);

        // 创建按钮并添加监听器
        JButton showGraphButton = new JButton("展示有向图");
        showGraphButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startAnalysis(1);
            }
        });
        topPanel.add(showGraphButton);

        JButton queryBridgeWordsButton = new JButton("查询桥接词");
        queryBridgeWordsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startAnalysis(2);
            }
        });
        topPanel.add(queryBridgeWordsButton);

        JButton generateTextButton = new JButton("根据桥接词生成新文本");
        generateTextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startAnalysis(3);
            }
        });
        topPanel.add(generateTextButton);

        JButton calcShortestPathButton = new JButton("计算两个单词之间的最短路径");
        calcShortestPathButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startAnalysis(4);
            }
        });
        topPanel.add(calcShortestPathButton);

        JButton randomWalkButton = new JButton("随机游走");
        randomWalkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startAnalysis(5);
            }
        });
        topPanel.add(randomWalkButton);

        JButton exitButton = new JButton("退出程序");
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startAnalysis(6);
            }
        });
        topPanel.add(exitButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TextAnalyzerVisualizer visualizer = new TextAnalyzerVisualizer();
            visualizer.setVisible(true);
        });
    }

    private void selectFile() {
        String projectDirectory = System.getProperty("user.dir");
        JFileChooser fileChooser = new JFileChooser(projectDirectory);
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            try {
                readTextFile(filePath);
                fileSelected = true; // 设置标志为 true，表示已选择文件
            } catch (IOException ex) {
                // 弹出错误提示
                JOptionPane.showMessageDialog(null, "Error reading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void startAnalysis(int choice) {
        if (!fileSelected) { // 如果没有选择文件，则弹出错误提示
            JOptionPane.showMessageDialog(null, "请先选择文件！", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        centerPanel.removeAll();
        centerPanel.repaint();
        JLabel label1 = new JLabel("请输入第一个单词：");
        JTextField textField1 = new JTextField(20);
        JLabel label2 = new JLabel("请输入第二个单词：");
        JTextField textField2 = new JTextField(20);
        JButton button = new JButton("查询");
        // 创建一个文本框来显示结果
        JTextArea resultTextArea = new JTextArea(null, 27, 100);
        resultTextArea.setEditable(false);
        // 使用 JScrollPane 包装 resultTextArea
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        switch (choice) {
            case 1:
                str="1 ";
                showDirectedGraph();
                break;
            case 2:
                centerPanel.add(label1);
                centerPanel.add(textField1);
                centerPanel.add(label2);
                centerPanel.add(textField2);
                centerPanel.add(button);
                centerPanel.add(scrollPane);
                // 添加按钮来触发操作
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String word1 = textField1.getText().toLowerCase();
                        String word2 = textField2.getText().toLowerCase();
                        str ="2 "+word1+" "+word2+" ";
                        resultTextArea.setText(queryBridgeWords(word1, word2));
                    }
                });
                break;
            case 3:
                str="3 ";
                label1.setText("请输入一行文本：");
                centerPanel.add(label1);
                textField1.setColumns(100);
                centerPanel.add(textField1);
                button.setText("生成新文本");
                centerPanel.add(button);
                centerPanel.add(scrollPane);
                // 添加按钮来触发操作
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String inputText = textField1.getText().toLowerCase();
                        resultTextArea.setText(generateNewText(inputText));
                    }
                });
                break;
            case 4:
                centerPanel.add(label1);
                centerPanel.add(textField1);
                centerPanel.add(label2);
                centerPanel.add(textField2);
                button.setText("计算最短路径");
                centerPanel.add(button);
                centerPanel.add(scrollPane);
                // 添加按钮来触发操作
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String word1 = textField1.getText().toLowerCase();
                        String word2 = textField2.getText().toLowerCase();
                        String result = "";
                        str="4 ";
                        // 如果用户只输入一个单词，则计算该单词到图中其他任一单词的最短路径
                        if (!graph.containsKey(word1) && !graph.containsKey(word2)) {   // 如果两个单词都不在图中
                            result = "No \"" + word1 + "\" and \"" + word2 + "\" in the graph!\n";
                        } else if (!graph.containsKey(word1) && graph.containsKey(word2)) { // 如果第一个单词不在图中
                            if (word1.isEmpty())    // 如果第一个单词为空
                                result = calcShortestPathFromWord(word2);
                            else    // 如果第一个单词不为空
                                result = "No \"" + word1 + "\" in the graph!\n";
                        } else if (graph.containsKey(word1) && !graph.containsKey(word2)) { // 如果第二个单词不在图中
                            if (word2.isEmpty())    // 如果第二个单词为空
                                result = calcShortestPathFromWord(word1);
                            else                        // 如果第二个单词不为空
                                result = "No \"" + word2 + "\" in the graph!\n";
                        } else                      // 如果两个单词都在图中
                            result = calcShortestPath(word1, word2);
                        resultTextArea.setText(result);
                    }
                });
                break;
            case 5:

                button.setText("开始随机游走");
                JButton stopButton = new JButton("停止随机游走");
                label1.setText("随机游走路径");
                JPanel buttonPanel = new JPanel(new GridBagLayout()); // 创建一个新的 JPanel，用于放置标签和按钮
                GridBagConstraints gbc = new GridBagConstraints();
                // 添加按钮
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.CENTER; // 居中对齐
                gbc.insets = new Insets(5, 5, 5, 5); // 设置边距
                buttonPanel.add(button, gbc);
                gbc.gridx = 1; // 放在第一行的下一列
                buttonPanel.add(stopButton, gbc);
                // 添加标签
                gbc.gridx = 0;
                gbc.gridy = 1; // 放在第二行
                gbc.gridwidth = 2; // 跨两列
                buttonPanel.add(label1, gbc);
                centerPanel.add(buttonPanel);
                centerPanel.add(scrollPane);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        str="5 ";
                        stop = false;
                        button.setEnabled(false); // 禁用开始按钮
                        stopButton.setEnabled(true); // 启用停止按钮
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String result = randomWalk();
                                resultTextArea.setText(result);
                                button.setEnabled(true); // 启用开始按钮
                                stopButton.setEnabled(false); // 禁用停止按钮
                            }
                        }).start();
                    }
                });
                stopButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        stop = true;
                    }
                });
                break;
            case 6:
                System.exit(0);
                break;
            default:
        }
        centerPanel.updateUI();
    }

    private static void readTextFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuilder textBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            // 将换行/回车符和标点符号替换为空格，将其他非字母字符忽略
            String cleanLine = line.replaceAll("[^a-zA-Z\\s]", " ");
            textBuilder.append(cleanLine).append(" ");
        }
        reader.close();
        String text = textBuilder.toString();
        String[] words = text.split("\\s+");
        // 处理每个单词对
        for (int i = 0; i < words.length - 1; i++) {
            String currentWord = words[i].toLowerCase();
            String nextWord = words[i + 1].toLowerCase();
            // 添加当前单词到图中
            if (!graph.containsKey(currentWord)) {
                graph.put(currentWord, new HashMap<>());
            }
            // 添加下一个单词到当前单词的邻居中
            HashMap<String, Integer> neighbors = graph.get(currentWord);
            neighbors.put(nextWord, neighbors.getOrDefault(nextWord, 0) + 1);
        }
        // 处理最后一个单词
        String k = words[words.length - 1];
        if (!graph.containsKey(k)) {
            graph.put(k, new HashMap<>());
        }
        for (String key : graph.keySet()) {
            map.put(key, graph.get(key));
        }
    }

    public static void showDirectedGraph() {
        StringBuilder graphText = new StringBuilder();
//        for (String word : graph.keySet()) {
//            graphText.append(word).append(" -> ");
//            HashMap<String, Integer> neighbors = graph.get(word);
//            for (String neighbor : neighbors.keySet()) {
//                graphText.append(neighbor).append("(").append(neighbors.get(neighbor)).append(") ");
//            }
//            graphText.append("\n");
//        }
        graphVisualization(map,str);    // 调用graphVisualization方法，传入map和str参数
        // 读取图像文件
        JFrame frame = new JFrame("Image Display");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(800, 600);  // 设置窗口大小
        String tmpDir = System.getProperty("user.dir"); //获取临时目录
        String graphFilePath = tmpDir+"/img/graph.png"; //生成dot文件路径
        try {
            // 读取图像文件
            Image image = ImageIO.read(new File(graphFilePath));
            // 将图像放入 ImageIcon
            ImageIcon imageIcon = new ImageIcon(image);
            // 使用 JLabel 显示 ImageIcon
            JLabel label = new JLabel(imageIcon);
            // 将 JLabel 放入 JScrollPane
            JScrollPane scrollPane = new JScrollPane(label);
            frame.add(scrollPane, BorderLayout.CENTER);

            // 显示窗口
            frame.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to load image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static String queryBridgeWords(String word1, String word2) {
        // 先检查输入的单词是否在图中
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No \"" + word1 + "\" or \"" + word2 + "\" in the graph!";
        }

        // 查询桥接词
        ArrayList<String> bridgeWords = new ArrayList<>();
        for (String word : graph.keySet()) {
            if (word.equals(word1) || word.equals(word2)) {// 跳过word1和word2本身
                continue; // 跳过word1和word2本身
            }
            // 如果word1和word2之间存在桥接词，则将word加入bridgeWords
            if (graph.get(word1).containsKey(word) && graph.containsKey(word) && graph.get(word).containsKey(word2)) {
                bridgeWords.add(word);
            }
        }
        if (bridgeWords.isEmpty()) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        } else {
            StringBuilder result = new StringBuilder("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" ");
            if (bridgeWords.size() == 1) {
                str+=bridgeWords.get(0);
                result.append("is: ").append(bridgeWords.get(0)).append(".");   // 输出桥接词
                showDirectedGraph();
            } else {
                result.append("are: ");
                for (int i = 0; i < bridgeWords.size(); i++) {
                    str+=bridgeWords.get(i);
                    str+=" ";
                    result.append(bridgeWords.get(i));
                    if (i < bridgeWords.size() - 2) {
                        result.append(", ");
                    } else if (i == bridgeWords.size() - 2) {
                        result.append(", and ");
                    }
                }
                showDirectedGraph();
                result.append(".");
            }
            return result.toString();
        }
    }

    public static String generateNewText(String inputText) {
        String[] words = inputText.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
        StringBuilder newText = new StringBuilder();
        // 遍历输入文本中的每一对相邻单词
        for (int i = 0; i < words.length - 1; i++) {
            String currentWord = words[i];
            String nextWord = words[i + 1];
            // 将当前单词添加到新文本中
            newText.append(currentWord).append(" ");
            // 查找当前单词与下一个单词之间的桥接词
            ArrayList<String> bridgeWords = findBridgeWords(currentWord, nextWord);
            // 如果存在桥接词，则随机选择一个插入到新文本中
            if (!bridgeWords.isEmpty()) {
                int randomIndex = new Random().nextInt(bridgeWords.size());
                newText.append(bridgeWords.get(randomIndex)).append(" ");
            }
        }
        // 添加最后一个单词到新文本中
        newText.append(words[words.length - 1]);
        return newText.toString();
    }

    private static ArrayList<String> findBridgeWords(String word1, String word2) {
        ArrayList<String> bridgeWords = new ArrayList<>();
        // 如果word1和word2都在图中出现，则查找桥接词
        if (graph.containsKey(word1) && graph.containsKey(word2)) {
            for (String word : graph.keySet()) {
                if (word.equals(word1) || word.equals(word2)) {
                    continue; // 跳过word1和word2本身
                }
                if (graph.get(word1).containsKey(word) && graph.containsKey(word)
                        && graph.get(word).containsKey(word2)) {
                    bridgeWords.add(word);
                }
            }
        }
        return bridgeWords;
    }

    private static String calcShortestPath(String word1, String word2) {
        // 使用Dijkstra算法计算最短路径
        HashMap<String, Integer> distance = new HashMap<>();
        HashMap<String, String> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(distance::get)); // 优先队列

        // 初始化距离和前驱
        for (String word : graph.keySet()) {    // 初始化距离和前驱
            distance.put(word, Integer.MAX_VALUE);  // 设置初始距离为无穷大
            previous.put(word, null);   // 设置初始前驱为空
        }
        distance.put(word1, 0); // 设置起始节点的距离为0
        pq.offer(word1);    // 将起始节点加入优先队列

        // Dijkstra算法
        while (!pq.isEmpty()) { // 优先队列不为空
            String current = pq.poll(); // 取出当前距离最小的节点
            if (visited.contains(current)) {    // 如果已经访问过，则跳过
                continue;
            }
            visited.add(current);    // 标记为已访问
            if (current.equals(word2)) {    // 如果找到目标单词，则退出循环
                break; // 找到目标单词，退出循环
            }
            HashMap<String, Integer> neighbors = graph.get(current);    // 获取当前节点的邻居
            if (neighbors != null) {    // 如果当前节点存在邻居
                for (String neighbor : neighbors.keySet()) {    // 遍历当前节点的邻居
                    Integer distToNeighbor = distance.get(neighbor);    // 获取邻居的距离
                    if (distToNeighbor == null) {    // 如果邻居不存在于图中，则跳过
                        continue; // 如果邻居不存在于图中，则跳过
                    }
                    int newDistance = distance.get(current) + neighbors.get(neighbor);  // 计算新距离
                    if (newDistance < distance.get(neighbor)) { // 如果新距离小于邻居的距离
                        distance.put(neighbor, newDistance);    // 更新邻居的距离
                        previous.put(neighbor, current);    // 更新邻居的前驱
                        pq.offer(neighbor);    // 将邻居加入优先队列
                    }
                }
            }
        }

        // 构造最短路径
        List<String> shortestPath = new ArrayList<>();    // 最短路径
        String current = word2; // 当前节点
        while (current != null) {    // 循环直到到达起始节点
            shortestPath.add(current);    // 将当前节点添加到最短路径
            current = previous.get(current);    // 移动到前驱节点
        }
        Collections.reverse(shortestPath);  // 反转最短路径

        // 构造输出字符串
        StringBuilder result = new StringBuilder(); // 输出字符串
        if (shortestPath.size() == 1) {  // 如果只有一个节点，则说明没有路径
            result.append("No path from \"").append(word1).append("\" to \"").append(word2).append("\"!");
        } else {        // 如果有路径
            result.append("Shortest path from \"").append(word1).append("\" to \"").append(word2).append("\": ");
            for (int i = 0; i < shortestPath.size(); i++) { // 遍历最短路径
                str += shortestPath.get(i)+" ";
                result.append(shortestPath.get(i)); // 输出节点
                if (i < shortestPath.size() - 1) {  // 输出箭头
                    result.append(" -> ");    // 输出箭头
                }
            }
            showDirectedGraph();
            result.append("\nLength of shortest path: ").append(distance.get(word2)).append("\n");        // 输出路径长度
        }
        return result.toString();
    }

    // 计算从某个单词到图中其他任一单词的最短路径
    private static String calcShortestPathFromWord(String word) {
        // 使用Dijkstra算法计算最短路径
        Map<String, Integer> distance = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(distance::get));

        // 初始化距离和前驱
        for (String w : graph.keySet()) {
            distance.put(w, Integer.MAX_VALUE);
            previous.put(w, null);
        }
        distance.put(word, 0);
        pq.offer(word);

        // Dijkstra算法
        while (!pq.isEmpty()) {
            String current = pq.poll();
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);
            HashMap<String, Integer> neighbors = graph.get(current);
            if (neighbors != null) {
                for (String neighbor : neighbors.keySet()) {
                    Integer distToNeighbor = distance.get(neighbor);
                    if (distToNeighbor == null) {
                        continue; // 如果邻居不存在于图中，则跳过
                    }
                    int newDistance = distance.get(current) + neighbors.get(neighbor);
                    if (newDistance < distToNeighbor) {
                        distance.put(neighbor, newDistance);
                        previous.put(neighbor, current);
                        pq.offer(neighbor);
                    }
                }
            }
        }

        // 构造输出字符串
        StringBuilder result = new StringBuilder("Shortest paths from \"").append(word).append("\":\n");
        for (String w : distance.keySet()) {
            if (!w.equals(word)) {
                result.append("To \"").append(w).append("\": ");
                List<String> shortestPath = new ArrayList<>();
                String current = w;
                while (current != null) {
                    shortestPath.add(current);
                    current = previous.get(current);
                }
                Collections.reverse(shortestPath);
                if (shortestPath.size() == 1) {
                    result.append("no path!\n");
                } else {
                    for (int i = 0; i < shortestPath.size(); i++) {
                        result.append(shortestPath.get(i));
                        if (i < shortestPath.size() - 1) {
                            result.append(" -> ");
                        }
                    }
                    result.append("\n    length of shortest path: ").append(distance.get(w)).append("\n");
                }
            }
        }
        return result.toString();
    }

    private static String randomWalk() {
        StringBuilder path = new StringBuilder();
        HashSet<String> visited = new HashSet<>();
        Random random = new Random();
        // 随机选择一个节点作为起点
        String currentWord = new ArrayList<>(graph.keySet()).get(random.nextInt(graph.size()));
        str+=currentWord+" ";
        path.append(currentWord).append(" ");
        // 开始随机游走
        while (graph.containsKey(currentWord) && !graph.get(currentWord).isEmpty() && !visited.contains(currentWord)) {
            if (stop) {
                break; // 如果 stop 为 true，立即退出循环
            }
            HashMap<String, Integer> neighbors = graph.get(currentWord);
            ArrayList<String> neighborWords = new ArrayList<>(neighbors.keySet());
            // 随机选择下一个节点
            String nextWord = neighborWords.get(random.nextInt(neighbors.size()));
            str+=nextWord+" ";
            path.append(nextWord).append(" ");
            visited.add(currentWord);
            currentWord = nextWord;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted. Cleaning up and exiting...");
            };
        }
        String result = path.toString().trim();

        // 将随机游走路径添加到out.txt文件
        try {
            Files.write(Paths.get("out.txt"), (result + "\n").getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error writing random walk result to file: " + e.getMessage());
        }
        showDirectedGraph();
        return result;
    }
    public static void graphVisualization(Map<String, Map<String, Integer>> input_graph, String str) {
        // 创建示例图
        Map<String, Map<String, Integer>> graph = new HashMap<>();
        graph = input_graph;
        // 进行深度优先搜索并输出图形化结果
        System.out.println("str:"+str);
        System.out.println("Graph Visualization:");
        writeDotFile(createDotFile(graph, str));
    }

    public static String createDotFile(Map<String, Map<String, Integer>> input_graph, String str) {
        char newLine = '\n';
        StringBuilder dotText = new StringBuilder();    //StringBuilder在这里效率要高于用String加加加
        dotText.append(String.format("digraph G{" + newLine));    //写入开头
        String[] words = str.split("\\s+");    //分割字符串
        int len=words.length;
        if(words[0].equals("2")){
            String[] subset = Arrays.copyOfRange(words, 3, len);
            System.out.println(Arrays.toString(subset));
            for (String node : input_graph.keySet()) {  //遍历节点
                dotText.append("\t").append(node);    //写入节点
                if (Objects.equals(node, words[1])||Objects.equals(node, words[2])) {  //判断是否为起始节点
                    dotText.append(" [style=filled, fillcolor=green]");    //设置节点颜色
                }
                if(Arrays.asList(subset).contains(node)){
                    dotText.append(" [style=filled, fillcolor=blue]");
                }
                dotText.append(";").append(newLine);    //写入节点属性
            }
            dotText.append(newLine);
            for (String node : input_graph.keySet()) {  //遍历节点
                Map<String, Integer> neighbors = input_graph.get(node);  //获取节点的邻居
                if (neighbors != null) {        //判断是否有邻居
                    for (String neighbor : neighbors.keySet()) {    //遍历邻居
                        int weight = neighbors.get(neighbor);    //获取边的权重
                        dotText.append("\t");    //写入边的起始节点
                        if(!Objects.equals(node, words[2])&&Arrays.asList(words).contains(node)&&Arrays.asList(words).contains(neighbor)){
                            dotText.append(String.format("%s->%s[label=\"%s\", color=\"%s\"]", node, neighbor, weight, "blue"));
                        }
                        else {
                            dotText.append(String.format("%s->%s[label=%d]", node, neighbor, weight));    //写入边的属性
                        }
                        dotText.append(";").append(newLine);            //写入边的结束符
                    }
                }
            }
        }
        else if(words[0].equals("4")) {
            int len1=words.length-1;
            dotText.append(String.format("    label=\"Length of shortest path from  \'%s\' to \'%s\':%d\";\n", words[1], words[len-1],len1-1));
            dotText.append("    node [shape=plaintext];\n");
            dotText.append("    blank [label=\" \"];\n");
            dotText.append("    node [shape=oval];\n");

            for (String node : input_graph.keySet()) {  //遍历节点    //写入节点
                dotText.append("\t").append(node);
                if (Objects.equals(node, words[1]) || Objects.equals(node, words[len-1]) ) {  //判断是否为起止节点
                    dotText.append(" [style=filled, fillcolor=green]");    //设置节点颜色
                }
                else if (Arrays.asList(words).contains(node)) {
                    dotText.append(" [style=filled, fillcolor=blue]");
                }
            }
            dotText.append(newLine);            //写入换行符
            //遍历写入边
            for (String node : input_graph.keySet()) {  //遍历节点
                Map<String, Integer> neighbors = input_graph.get(node);  //获取节点的邻居
                if (neighbors != null) {        //判断是否有邻居
                    for (String neighbor : neighbors.keySet()) {    //遍历邻居
                        int weight = neighbors.get(neighbor);    //获取边的权重
                        dotText.append("\t");    //写入边的起始节点
                        if(!Objects.equals(node, words[len-1])&&Arrays.asList(words).contains(node)&&Arrays.asList(words).contains(neighbor)){
                            dotText.append(String.format("%s->%s[label=\"%s\", color=\"%s\"]", node, neighbor, weight, "blue"));
                        }
                        else {
                            dotText.append(String.format("%s->%s[label=%d]", node, neighbor, weight));    //写入边的属性
                        }
                        dotText.append(";").append(newLine);            //写入边的结束符
                    }
                }
            }
        }
        else if(words[0].equals("5")) {
            for (String node : input_graph.keySet()) {  //遍历节点    //写入节点
                dotText.append("\t").append(node);
                if (Objects.equals(node, words[1]) || Objects.equals(node, words[len-1]) ) {  //判断是否为起止节点
                    dotText.append(" [style=filled, fillcolor=green]");    //设置节点颜色
                }
                else if (Arrays.asList(words).contains(node)) {
                    dotText.append(" [style=filled, fillcolor=blue]");
                }
            }
            dotText.append(newLine);            //写入换行符
            //遍历写入边
            for (String node : input_graph.keySet()) {  //遍历节点
                Map<String, Integer> neighbors = input_graph.get(node);  //获取节点的邻居
                if (neighbors != null) {        //判断是否有邻居
                    for (String neighbor : neighbors.keySet()) {    //遍历邻居
                        int weight = neighbors.get(neighbor);    //获取边的权重
                        dotText.append("\t");    //写入边的起始节点
                        if(Arrays.asList(words).contains(node)&&Arrays.asList(words).contains(neighbor)){
                            dotText.append(String.format("%s->%s[label=\"%s\", color=\"%s\"]", node, neighbor, weight, "blue"));
                        }
                        else {
                            dotText.append(String.format("%s->%s[label=%d]", node, neighbor, weight));    //写入边的属性
                        }
                        dotText.append(";").append(newLine);            //写入边的结束符
                    }
                }
            }
        }
        else{
            for (String node : input_graph.keySet()) {  //遍历节点
                dotText.append("\t").append(node);    //写入节点名

                if (Objects.equals(node, str)) {  //判断是否为起始节点
                    dotText.append(" [style=filled, fillcolor=red]");    //设置节点颜色
                }
                dotText.append(";").append(newLine);    //写入节点属性
            }
            dotText.append(newLine);            //写入换行符
            //遍历写入边
            for (String node : input_graph.keySet()) {  //遍历节点
                Map<String, Integer> neighbors = input_graph.get(node);  //获取节点的邻居
                if (neighbors != null) {        //判断是否有邻居
                    for (String neighbor : neighbors.keySet()) {    //遍历邻居
                        int weight = neighbors.get(neighbor);    //获取边的权重
                        dotText.append("\t");    //写入边的起始节点
                        dotText.append(String.format("%s->%s[label=%d]", node, neighbor, weight));    //写入边的属性
                        dotText.append(";").append(newLine);            //写入边的结束符
                    }
                }
            }
        }


        dotText.append("}").append(newLine);    //写入结束
        return dotText.toString();    //返回dot文件内容
    }

    public static void writeDotFile(String dotText) {
        String tmpDir = System.getProperty("user.dir"); //获取临时目录
        String graphFilePath = tmpDir + "/img/graph.gv"; //生成dot文件路径
        System.out.println(tmpDir);
        try {    //写入dot文件
            File tmpfile = new File(tmpDir);    //创建临时目录
            if (!tmpfile.exists()) {        //判断目录是否存在，不存在则创建
                tmpfile.mkdirs();    //创建目录
            }
            FileWriter fw = new FileWriter(graphFilePath);    //创建文件写入器
            System.out.println("Writing graph to " + graphFilePath);    //输出文件路径
            BufferedWriter bufWriter = new BufferedWriter(fw);
            bufWriter.write(dotText);    //写入文件内容
            bufWriter.close();    //关闭文件写入器
        } catch (Exception e) {    //捕获异常
            throw new RuntimeException("Failed to open file");    //抛出运行时异常
        }
        runGraphViz(graphFilePath,tmpDir);    //调用GraphViz生成图片
    }
    public static void runGraphViz(String filename,String tmpDir){
        Runtime rt=Runtime.getRuntime();	//使用Runtime执行cmd命令
        try {
            String dotForWindows=tmpDir+"/Graphviz-11.0.0-win64/bin/dot.exe";
            String[] args= {dotForWindows,filename,"-Tpng","-o",tmpDir+"/img/graph.png"};  //执行命令行参数
            Process process = rt.exec(args);    //执行命令
            process.waitFor();    //等待命令执行完成

        }catch (Exception e) {    //捕获异常
            throw new RuntimeException("Failed to generate image.");    //抛出运行时异常
        }
    }
}
