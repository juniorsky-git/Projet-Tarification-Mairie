# =========================
# VARIABLES
# =========================

# Séparateur de classpath selon l'OS
ifeq ($(OS),Windows_NT)
	SEP = ;
else
	SEP = :
endif

# Java
JC      = javac
JVM     = java
JAR     = jar

# Chemins
SRC_PATH   = src/fr/mairie/tarification/
BUILD_PATH = build/fr/mairie/tarification/
DIST_PATH  = dist/

# Package et classe principale
PACKAGE_NAME = fr.mairie.tarification
MAIN_CLASS   = fr.mairie.tarification.Main

# Librairies Apache POI (tous les JARs dans lib/)
LIB_JARS = lib/poi-5.3.0.jar$(SEP)lib/poi-ooxml-5.3.0.jar$(SEP)lib/poi-ooxml-lite-5.3.0.jar$(SEP)lib/commons-collections4-4.4.jar$(SEP)lib/commons-compress-1.26.1.jar$(SEP)lib/commons-io-2.16.1.jar$(SEP)lib/log4j-api-2.23.1.jar$(SEP)lib/SparseBitSet-1.3.jar$(SEP)lib/xmlbeans-5.2.1.jar

# Flags
JCFLAGS  = -encoding UTF-8 -d build -cp "build$(SEP)$(LIB_JARS)" -sourcepath src
JVMFLAGS = -cp "build$(SEP)$(LIB_JARS)"

# Fichiers sources et cibles
SOURCES = $(SRC_PATH)Tarif.java \
          $(SRC_PATH)DonneesTarifs.java \
          $(SRC_PATH)TarificationService.java \
          $(SRC_PATH)ExcelReader.java \
          $(SRC_PATH)Calculateur.java \
          $(SRC_PATH)ConsoleUI.java \
          $(SRC_PATH)Main.java

CLASSES = $(BUILD_PATH)Tarif.class \
          $(BUILD_PATH)DonneesTarifs.class \
          $(BUILD_PATH)TarificationService.class \
          $(BUILD_PATH)ExcelReader.class \
          $(BUILD_PATH)Calculateur.class \
          $(BUILD_PATH)ConsoleUI.class \
          $(BUILD_PATH)Main.class

JAR_APP = $(DIST_PATH)tarification.jar

# =========================
# CIBLE PAR DEFAUT
# =========================
all: jar

# =========================
# CREATION DOSSIERS
# =========================
builddir:
ifeq ($(OS),Windows_NT)
	@if not exist build mkdir build
else
	mkdir -p build
endif

distdir:
ifeq ($(OS),Windows_NT)
	@if not exist dist mkdir dist
else
	mkdir -p $(DIST_PATH)
endif

# =========================
# COMPILATION DES CLASSES
# =========================

# Modèle de données
$(BUILD_PATH)Tarif.class: $(SRC_PATH)Tarif.java | builddir
	$(JC) $(JCFLAGS) $(SRC_PATH)Tarif.java

# Données tarifaires (dépend de Tarif et ExcelReader)
$(BUILD_PATH)DonneesTarifs.class: $(SRC_PATH)DonneesTarifs.java $(BUILD_PATH)Tarif.class $(BUILD_PATH)ExcelReader.class
	$(JC) $(JCFLAGS) $(SRC_PATH)DonneesTarifs.java

# Service de calcul (dépend de Tarif)
$(BUILD_PATH)TarificationService.class: $(SRC_PATH)TarificationService.java $(BUILD_PATH)Tarif.class
	$(JC) $(JCFLAGS) $(SRC_PATH)TarificationService.java

# Lecteur Excel Apache POI (dépend de Tarif et DonneesTarifs)
$(BUILD_PATH)ExcelReader.class: $(SRC_PATH)ExcelReader.java $(BUILD_PATH)Tarif.class
	$(JC) $(JCFLAGS) $(SRC_PATH)ExcelReader.java

# Calculateur automatique
$(BUILD_PATH)Calculateur.class: $(SRC_PATH)Calculateur.java $(BUILD_PATH)DonneesTarifs.class
	$(JC) $(JCFLAGS) $(SRC_PATH)Calculateur.java

# Interface Console Épurée
$(BUILD_PATH)ConsoleUI.class: $(SRC_PATH)ConsoleUI.java
	$(JC) $(JCFLAGS) $(SRC_PATH)ConsoleUI.java

# Point d'entrée (dépend de tout)
$(BUILD_PATH)Main.class: $(SRC_PATH)Main.java \
	$(BUILD_PATH)Tarif.class \
	$(BUILD_PATH)DonneesTarifs.class \
	$(BUILD_PATH)TarificationService.class \
	$(BUILD_PATH)ExcelReader.class \
	$(BUILD_PATH)Calculateur.class \
	$(BUILD_PATH)ConsoleUI.class
	$(JC) $(JCFLAGS) $(SRC_PATH)Main.java

# =========================
# JAR EXECUTABLE -> dist/
# =========================
$(JAR_APP): distdir $(BUILD_PATH)Main.class
	$(JAR) cfe $(JAR_APP) $(MAIN_CLASS) -C build fr

jar: $(JAR_APP)

# =========================
# EXECUTION
# =========================
run: $(BUILD_PATH)Main.class
	$(JVM) $(JVMFLAGS) $(MAIN_CLASS)

run-jar: $(JAR_APP)
	$(JVM) -cp "$(JAR_APP)$(SEP)$(LIB_JARS)" $(MAIN_CLASS)

# =========================
# JAVADOC
# =========================
javadoc:
ifeq ($(OS),Windows_NT)
	@if not exist javadoc mkdir javadoc
else
	mkdir -p javadoc
endif
	javadoc -encoding UTF-8 -d javadoc -sourcepath src -subpackages fr.mairie.tarification -classpath "$(LIB_JARS)"

# =========================
# NETTOYAGE
# =========================
clean:
ifeq ($(OS),Windows_NT)
	-rmdir /S /Q build
	-rmdir /S /Q dist
else
	-rm -rf build
	-rm -rf dist
endif

cleanjavadoc:
ifeq ($(OS),Windows_NT)
	-rmdir /S /Q javadoc
else
	-rm -rf javadoc
endif

cleanall: clean cleanjavadoc

# =========================
# BUTS FACTICES
# =========================
.PHONY: all jar run run-jar javadoc clean cleanjavadoc cleanall builddir distdir
