// Importation des bibliothèques nécessaires
import javax.swing.*;  // Pour l'interface graphique (fenêtres, boutons, menus)
import java.awt.*;   // Pour les couleurs, layout, composants graphiques
import java.awt.event.*; // Pour la gestion des événements (souris, clavier)
import java.awt.geom.Line2D;  // Pour manipuler les lignes et calculs de distance
import java.io.*;   // Pour la sérialisation (sauvegarde/chargement)
import java.util.*;  // Pour les collections comme List, ArrayList
import java.util.List;


// Classe principale 
public class PooProjet extends JFrame {
    private final PaintGraphe panel;  // Panneau de dessin du graphe

    public PooProjet() {
        super("PROJET POO1");
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Fermer l'application à la fermeture de la fenêtre
        setSize(900, 600);  // Taille de la fenêtre
        setLocationRelativeTo(null);

        panel = new PaintGraphe();   // Création du panneau de dessin
        add(panel, BorderLayout.CENTER);  // Ajout du panneau au centre de la fenêtre

        JMenuBar menuBar = new JMenuBar();  // Barre de menu


  // Menu Fichier avec options Enregistrer et Charger
        JMenu fileMenu = new JMenu("Fichier");
        JMenuItem save = new JMenuItem("Enregistrer");
        JMenuItem load = new JMenuItem("Charger");
        save.addActionListener(e -> panel.sauvegarderGraphe());  // Action de sauvegarde
        load.addActionListener(e -> panel.chargerGraphe());    // Action de chargement
        fileMenu.add(save);
        fileMenu.add(load);

// Menu pour créer un lien entre tâches
        JMenu linkMenu = new JMenu("Lien");
        JMenuItem createLink = new JMenuItem("Créer un lien");
        createLink.addActionListener(e -> panel.creerLienParNom());
        linkMenu.add(createLink);

  // Menu Aide pour afficher les instructions
        JMenu helpMenu = new JMenu("Aide");
        JMenuItem helpItem = new JMenuItem("Instructions");
        helpItem.addActionListener(e -> afficherAide());
        helpMenu.add(helpItem);


 // Ajout des menus à la barre de menu
        menuBar.add(fileMenu);
        menuBar.add(linkMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar); // Mise en place de la barre de menu
        setVisible(true);    // Rend la fenêtre visible
    }


    // Affiche les instructions d'aide
    private void afficherAide() {
        String message = """
                ▸ Pour créer un sommet :
                  - Cliquez droit sur une zone vide.
                  - Entrez un nom, une durée (entier), et des contraintes (noms séparés par des virgules ou laissez vide ' Par defaut sera debut').
                ▸ Pour créer un lien :
                  - Menu "Lien" > "Créer un lien" (par noms).
                ▸ Pour modifier un sommet :
                  - Cliquez droit sur le sommet, puis choisissez "Modifier".
                ▸ Pour supprimer un sommet :
                  - Cliquez droit sur le sommet, puis choisissez "Supprimer".
                  - Attention : cela supprime aussi ses liens.
                ▸ Pour supprimer un lien :
                  - Cliquez gauche sur la ligne du lien, puis validez la suppression.
                ▸ Déplacez un sommet par glisser-déposer (clic gauche).
                ▸ Quant à la tâche 'fin', c'est à l'utilisateur de créer manuellement un lien depuis la dernière tâche vers 'fin'
                ▸ Les tâches critiques sont en rouge.
                
                """;
        JOptionPane.showMessageDialog(this, message, "Aide", JOptionPane.INFORMATION_MESSAGE);
    }


 // Méthode principale qui lance l'application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(PooProjet::new);
    }
}


// Classe représentant le panneau de dessin du graphe
class PaintGraphe extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    private final Graph graph = new Graph();
    private Task selectedTask = null;
    private Point dragOffset = null;

    public PaintGraphe() {
        setBackground(Color.WHITE);  // Fond blanc
        addMouseListener(this);   // pour les clics
        addMouseMotionListener(this);  // pour les mouvements de souris
        setFocusable(true);  // Peut recevoir les événements clavier
        addKeyListener(this);   // pour les touches
        graph.ajouterDebutEtFin(); // Ajoute les nœuds 'debut' et 'fin'
    }
// Dessine le graphe
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Task task : graph.getTasks()) {
            for (Task pred : task.getPredecessors()) {
                g.setColor(Color.BLACK);
                g.drawLine(pred.getX(), pred.getY(), task.getX(), task.getY());  // ligne entre prédécesseur et tâche
                int mx = (pred.getX() + task.getX()) / 2;
                int my = (pred.getY() + task.getY()) / 2;
                g.setColor(Color.BLUE);
                g.drawString(String.valueOf(pred.getDuration()), mx, my - 5);  // Affiche la durée
            }
        }


        // Dessine les cercles représentant les tâches
        for (Task task : graph.getTasks()) {
            g.setColor(task.isCritique() ? Color.RED : Color.CYAN);  // Couleur rouge si critique
            g.fillOval(task.getX() - 20, task.getY() - 20, 40, 40);   // Cercle rempli
            g.setColor(Color.BLACK);
            g.drawOval(task.getX() - 20, task.getY() - 20, 40, 40);   // Contour du cercle
            g.drawString(task.getName(), task.getX() - 10, task.getY() + 5);   // Nom de la tâche
        }
    }


// Création d’un lien entre deux tâches à partir de leurs noms
    public void creerLienParNom() {
        String fromName = JOptionPane.showInputDialog(this, "Nom de la tâche source :");
        if (fromName == null) return;
        String toName = JOptionPane.showInputDialog(this, "Nom de la tâche cible :");
        if (toName == null) return;

        Task from = graph.getTaskByName(fromName.trim());
        Task to = graph.getTaskByName(toName.trim());

        if (from == null || to == null || from == to) {
            JOptionPane.showMessageDialog(this, "Tâche introuvable ou lien invalide.");
            return;
        }

        to.addPredecessor(from);  // Ajout du lien
        recalculer();   // Recalcul des dates
    }


// Recalcule les dates au plus tôt et au plus tard
    private void recalculer() {
        graph.calculateDatesTot();
        graph.calculateDatesTard();
        repaint();  // Redessine le graphe
    }



 // Sauvegarde le graphe dans un fichier
    public void sauvegarderGraphe() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(chooser.getSelectedFile()))) {
                out.writeObject(graph);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }




// Charge un graphe depuis un fichier
    public void chargerGraphe() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(chooser.getSelectedFile()))) {
                Graph loaded = (Graph) in.readObject();
                graph.setTasks(loaded.getTasks());
                recalculer();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }



// Gestion des clics de souris
    @Override
    public void mousePressed(MouseEvent e) {
        requestFocusInWindow();  // Nécessaire pour capter le clavier
        Task clicked = getTaskAt(e.getPoint());   // Vérifie si une tâche est cliquée

        if (SwingUtilities.isLeftMouseButton(e)) {
            if (clicked != null) {
                // Début du glisser-déposer
                selectedTask = clicked;
                dragOffset = new Point(e.getX() - clicked.getX(), e.getY() - clicked.getY());
            } else {
                // Test si clic sur une ligne : propose de la supprimer
                for (Task t1 : graph.getTasks()) {
                    for (Task t2 : graph.getTasks()) {
                        if (t2.getPredecessors().contains(t1)) {
                            Point p1 = new Point(t1.getX(), t1.getY());
                            Point p2 = new Point(t2.getX(), t2.getY());
                            if (isPointNearLine(e.getPoint(), p1, p2)) {
                                int res = JOptionPane.showConfirmDialog(this, "Supprimer ce lien ?", "Confirmation", JOptionPane.YES_NO_OPTION);
                                if (res == JOptionPane.YES_OPTION) {
                                    t2.getPredecessors().remove(t1);
                                    recalculer();
                                }
                                return;
                            }
                        }
                    }
                }
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            if (clicked != null) {
                 // Menu contextuel : modifier ou supprimer la tâche
                JPopupMenu popup = new JPopupMenu();

                JMenuItem modifier = new JMenuItem("Modifier");
                modifier.addActionListener(ev -> modifierTache(clicked));

                JMenuItem supprimer = new JMenuItem("Supprimer");
                supprimer.addActionListener(ev -> {
                    graph.removeTask(clicked);
                    recalculer();
                });

                popup.add(modifier);
                popup.add(supprimer);
                popup.show(this, e.getX(), e.getY());

            } else {
                // Création d'une nouvelle tâche
                String name = JOptionPane.showInputDialog(this, "Nom de la tâche :");
                if (name == null || name.trim().isEmpty()) return;

                String durationStr = JOptionPane.showInputDialog(this, "Durée (entier) :");
                if (durationStr == null) return;

                int duration;
                try {
                    duration = Integer.parseInt(durationStr.trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Durée invalide.");
                    return;
                }

                String constraints = JOptionPane.showInputDialog(this, "Contraintes (noms prédécesseurs, séparés par virgule ou vide) :");
                Task task = new Task(name.trim(), duration, e.getX(), e.getY());
                graph.addTask(task);

                if (constraints != null && !constraints.trim().isEmpty()) {
                    String[] preds = constraints.split(",");
                    for (String predName : preds) {
                        Task pred = graph.getTaskByName(predName.trim());
                        if (pred != null) {
                            task.addPredecessor(pred);
                        }
                    }
                } else {
                    Task debut = graph.getTaskByName("debut");
                    if (debut != null) {
                        task.addPredecessor(debut);
                    }
                }

                recalculer();
            }
        }
    }



 // Modifie une tâche existante
    private void modifierTache(Task t) {
        String newName = JOptionPane.showInputDialog(this, "Nouveau nom :", t.getName());
        if (newName != null && !newName.trim().isEmpty()) {
            t.setName(newName.trim());
        }

        String durationStr = JOptionPane.showInputDialog(this, "Nouvelle durée :", t.getDuration());
        if (durationStr != null) {
            try {
                t.setDuration(Integer.parseInt(durationStr.trim()));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Durée invalide.");
            }
        }
        recalculer();
    }



   // Vérifie si un point est proche d’une ligne
    private boolean isPointNearLine(Point p, Point a, Point b) {
        final double threshold = 5.0;
        double dist = Line2D.ptSegDist(a.x, a.y, b.x, b.y, p.x, p.y);
        return dist < threshold;
    }


// Retourne la tâche située à un point donné (si elle existe)
    private Task getTaskAt(Point p) {
        for (Task task : graph.getTasks()) {
            if (p.distance(task.getX(), task.getY()) < 20) return task;
        }
        return null;
    }


  // Événements inutilisés
    @Override public void mouseReleased(MouseEvent e) { selectedTask = null; dragOffset = null; }
    @Override public void mouseDragged(MouseEvent e) {
        if (selectedTask != null && dragOffset != null) {
            selectedTask.setX(e.getX() - dragOffset.x);
            selectedTask.setY(e.getY() - dragOffset.y);
            repaint();
        }
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE && selectedTask != null) {
            graph.removeTask(selectedTask);
            selectedTask = null;
            recalculer();
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}



// Classe représentant une tâche
class Task implements Serializable {
    private String name;
    private int duration;
    private int x, y;
    private final List<Task> predecessors = new ArrayList<>();
    private int dateTot = 0;
    private int dateTard = Integer.MAX_VALUE;

    public Task(String name, int duration, int x, int y) {
        this.name = name;
        this.duration = duration;
        this.x = x;
        this.y = y;
    }

    public void addPredecessor(Task pred) {
        if (!predecessors.contains(pred)) {
            predecessors.add(pred);
        }
    }


 // Getters et setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getDuration() { return duration; }
    public void setDuration(int d) { this.duration = d; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public List<Task> getPredecessors() { return predecessors; }

    public int getDateTot() { return dateTot; }
    public void setDateTot(int dateTot) { this.dateTot = dateTot; }

    public int getDateTard() { return dateTard; }
    public void setDateTard(int dateTard) { this.dateTard = dateTard; }

    public boolean isCritique() {
        return dateTot == dateTard;
    }
}



// Classe qui contient toutes les tâches et calcule les dates
class Graph implements Serializable {
    private List<Task> tasks = new ArrayList<>();

    public void addTask(Task t) { tasks.add(t); }

    public void removeTask(Task t) {
        tasks.remove(t);
        for (Task task : tasks) {
            task.getPredecessors().remove(t);
        }
    }

    public List<Task> getTasks() { return tasks; }

    public void setTasks(List<Task> t) { this.tasks = t; }

    public Task getTaskByName(String name) {
        for (Task t : tasks) {
            if (t.getName().equalsIgnoreCase(name)) return t;
        }
        return null;
    }

    public void ajouterDebutEtFin() {
        Task debut = new Task("debut", 0, 50, 50);
        Task fin = new Task("fin", 0, 800, 500);
        tasks.add(debut);
        tasks.add(fin);
    }




// Calcul des dates au plus tôt
    public void calculateDatesTot() {
        for (Task task : tasks) {
            int max = 0;
            for (Task pred : task.getPredecessors()) {
                max = Math.max(max, pred.getDateTot() + pred.getDuration());
            }
            task.setDateTot(max);
        }
    }



// Calcul des dates au plus tard
    public void calculateDatesTard() {
        int maxTot = 0;
        for (Task task : tasks) {
            maxTot = Math.max(maxTot, task.getDateTot() + task.getDuration());
        }

        for (Task task : tasks) {
            task.setDateTard(maxTot);
        }

        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = tasks.get(i);
            for (Task succ : getSuccessors(task)) {
                task.setDateTard(Math.min(task.getDateTard(), succ.getDateTard() - task.getDuration()));
            }
        }
    }



// Retourne la liste des successeurs d'une tâche
    private List<Task> getSuccessors(Task task) {
        List<Task> successors = new ArrayList<>();
        for (Task t : tasks) {
            if (t.getPredecessors().contains(task)) {
                successors.add(t);
            }
        }
        return successors;
    }
}
