const { q, writeSubject } = require('./qgen_helper');

const chapters = [
  ["Pediatrics General", [
    q("Normal birth weight of an Indian newborn is approximately:", "2.5 to 3.5 kg", "1.5 to 2.5 kg", "3.5 to 4.5 kg", "1.0 to 1.5 kg", "A", "Low Birth Weight (LBW) is defined as <2.5 kg regardless of gestational age.", "EASY", "Textbook", "Neonatology"),
    q("A newborn infant is assessed at 1 minute of life: Heart rate 110, strong cry, active motion, completely pink, crying with stimulation. The Apgar score is:", "10", "8", "6", "9", "A", "HR >100 (2), Good respiratory effort (2), Active motion (2), Pink (2), Grimace/Cry (2) = 10.", "MEDIUM", "Textbook", "Neonatology"),
    q("The most common cause of respiratory distress syndrome (RDS) in a premature infant is:", "Surfactant deficiency", "Meconium aspiration", "Transient tachypnea of the newborn", "Congenital pneumonia", "A", "Lack of surfactant from Type II pneumocytes leads to alveolar collapse and hyaline membrane disease.", "EASY", "Textbook", "Neonatology"),
    q("Physiological jaundice in a full-term newborn typically peaks on day:", "3 to 4", "1 to 2", "7 to 10", "14 to 21", "A", "It is caused by shorter RBC lifespan and immature hepatic UDP-glucuronyltransferase.", "EASY", "Textbook", "Neonatology"),
    q("Which of the following congenital heart defects is a cyanotic defect?", "Tetralogy of Fallot", "Ventricular Septal Defect (VSD)", "Atrial Septal Defect (ASD)", "Patent Ductus Arteriosus (PDA)", "A", "Right-to-Left shunts bypass the lungs, causing cyanosis. TOF is the most common cyanotic CHD.", "EASY", "Textbook", "Cardiology"),
    q("The classic presentation of Intussusception in an infant is:", "Colicky abdominal pain, drawing up legs, and 'red currant jelly' stools", "Projectile non-bilious vomiting immediately after feeding", "Painless lower GI bleeding", "Bilious vomiting and failure to pass meconium", "A", "The telescoping bowel causes ischemia and mucosal sloughing (jelly stools).", "EASY", "Textbook", "Gastroenterology"),
    q("A 4-week-old male infant presents with projectile, non-bilious vomiting immediately after every feed. He is hungry after vomiting. A definitive physical finding is:", "A palpable 'olive-shaped' mass in the epigastrium", "A grossly distended abdomen", "Hyperactive bowel sounds", "Bloody diarrhea", "A", "This represents hypertrophic pyloric stenosis, requiring a pyloromyotomy (Ramstedt operation).", "EASY", "Textbook", "Gastroenterology"),
    q("The most common cause of acute bronchiolitis in infants under 1 year of age is:", "Respiratory Syncytial Virus (RSV)", "Parainfluenza virus", "Influenza virus", "Adenovirus", "A", "RSV infection causes widespread inflammation and necrosis of bronchiolar epithelium.", "EASY", "Textbook", "Respiratory"),
    q("A 3-year-old boy presents with high fever, a 'barking' seal-like cough, and inspiratory stridor. An AP neck X-ray shows the 'steeple sign'. The diagnosis is:", "Croup (Laryngotracheobronchitis)", "Epiglottitis", "Bacterial tracheitis", "Foreign body aspiration", "A", "Typically caused by Parainfluenza virus. Subglottic narrowing causes the steeple sign.", "MEDIUM", "Textbook", "Respiratory"),
    q("Which vitamin deficiency causes Rickets in children, characterized by craniotabes, rachitic rosary, and bowed legs?", "Vitamin D", "Vitamin C", "Vitamin A", "Vitamin B12", "A", "Vitamin D deficiency impairs calcium and phosphate absorption, leading to defective bone mineralization.", "EASY", "Textbook", "Nutrition")
  ]]
];

writeSubject("Pediatrics", chapters, "pediatrics.json");
