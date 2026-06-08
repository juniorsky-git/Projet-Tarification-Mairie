/**
 * Script de conversion Excel → JSON
 * Usage: node convertir-excel-en-json.js
 * Prérequis: npm install xlsx
 *
 * Ce script extrait UNIQUEMENT les données utiles des fichiers Excel
 * et les sauvegarde en fichiers JSON légers pour Railway.
 */

const XLSX = require('xlsx');
const fs = require('fs');
const path = require('path');

const RESOURCES = path.join(__dirname, 'tarification-api', 'src', 'main', 'resources');

function lireExcel(nomFichier) {
    const chemin = path.join(RESOURCES, nomFichier);
    if (!fs.existsSync(chemin)) {
        console.error(`❌ Fichier introuvable: ${chemin}`);
        process.exit(1);
    }
    console.log(`📂 Lecture: ${nomFichier}`);
    return XLSX.readFile(chemin, { cellFormula: false, cellHTML: false, sheetStubs: false });
}

function getVal(sheet, row, col) {
    const addr = XLSX.utils.encode_cell({ r: row, c: col });
    const cell = sheet[addr];
    if (!cell) return 0;
    const v = cell.v;
    if (typeof v === 'number') return v;
    if (typeof v === 'string') {
        const n = parseFloat(v.replace(',', '.').replace(/[^0-9.\-]/g, ''));
        return isNaN(n) ? 0 : n;
    }
    return 0;
}

function getStr(sheet, row, col) {
    const addr = XLSX.utils.encode_cell({ r: row, c: col });
    const cell = sheet[addr];
    if (!cell) return '';
    return String(cell.v || '').trim();
}

// ============================================================
// 1. CALC DEP (3).xlsx  →  synthese.json
// ============================================================
function convertirSynthese() {
    const wb = lireExcel('CALC DEP (3).xlsx');
    const sheet = wb.Sheets['syntheses charges'];
    if (!sheet) { console.error('Onglet "syntheses charges" introuvable'); return; }

    const poles = ['Restauration', 'Accueil de Loisirs', 'Accueil periscolaire', 'Etudes surveillees', 'Espace Ados', 'Sejours'];
    const colIndices = [3, 4, 5, 6, 7, 8]; // colonnes D à I (0-indexé)

    const depenses = {};
    const totauxDepenses = {};
    const effectifs = {};
    const tarifs = {};

    // Lignes 4 à 21 (index 3 à 20) : charges
    for (let i = 3; i <= 20; i++) {
        let nature = getStr(sheet, i, 1);
        if (!nature) nature = getStr(sheet, i, 0);
        if (!nature) continue;

        poles.forEach((pole, j) => {
            const montant = Math.abs(getVal(sheet, i, colIndices[j]));
            if (montant > 0) {
                if (!depenses[pole]) depenses[pole] = {};
                depenses[pole][nature] = montant;
            }
        });
    }

    // Ligne 22 (index 21) : totaux
    poles.forEach((pole, j) => {
        totauxDepenses[pole] = getVal(sheet, 21, colIndices[j]);
    });

    // Lignes 30 à 39 (index 29 à 38) : effectifs et tarifs
    for (let i = 29; i <= 38; i++) {
        let tranche = getStr(sheet, i, 1);
        if (!tranche) tranche = getStr(sheet, i, 0);
        if (!tranche) continue;

        effectifs[tranche] = getVal(sheet, i, 2);
        tarifs[tranche] = {};
        poles.forEach((pole, j) => {
            tarifs[tranche][pole] = getVal(sheet, i, colIndices[j]);
        });
    }

    const result = { depenses, totauxDepenses, effectifs, tarifs };
    const dest = path.join(RESOURCES, 'synthese.json');
    fs.writeFileSync(dest, JSON.stringify(result, null, 2));
    console.log(`✅ synthese.json écrit (${poles.length} pôles, ${Object.keys(effectifs).length} tranches)`);
}

// ============================================================
// 2. CALC DEP(4).xlsx  →  fluides.json + simulation.json
// ============================================================
function convertirCalcDep4() {
    const wb = lireExcel('CALC DEP(4).xlsx');

    // --- FLUIDES (onglets Conso eau, CONSO GAZ, CONSO ELEC) ---
    const fluides = { eau: [], gaz: [], elec: [], biSemestriel: [] };

    // EAU
    const sheetEau = wb.Sheets['Conso eau'];
    if (sheetEau) {
        const range = XLSX.utils.decode_range(sheetEau['!ref'] || 'A1');
        for (let i = 5; i <= range.e.r; i++) {
            const s1 = (getStr(sheetEau, i, 1) + ' ' + getStr(sheetEau, i, 2) + ' ' + getStr(sheetEau, i, 3)).trim();
            if (!s1) continue;
            const consoS1 = getVal(sheetEau, i, 7);
            const consoS2 = getVal(sheetEau, i, 17);
            const reelS1  = getVal(sheetEau, i, 8);
            const reelS2  = getVal(sheetEau, i, 18);
            const conso = consoS1 + consoS2;
            const reel  = reelS1 + reelS2;
            if (conso > 0 || reel > 0) {
                fluides.eau.push({ site: s1, conso, reel, consoS1, consoS2, reelS1, reelS2 });
            }
        }
        console.log(`  → Eau: ${fluides.eau.length} sites`);
    }

    // GAZ & ELEC — parcours par période
    function agreger(sheet, colSite, anneeStr) {
        const acc = {};
        if (!sheet) return acc;
        const range = XLSX.utils.decode_range(sheet['!ref'] || 'A1');
        let siteCourant = '';
        for (let i = 9; i <= range.e.r; i++) {
            const siteNom = getStr(sheet, i, colSite).trim();
            if (siteNom) siteCourant = siteNom;
            if (!siteCourant) continue;
            if (!acc[siteCourant]) acc[siteCourant] = { totalConso: 0, totalReel: 0, consoS1: 0, consoS2: 0, reelS1: 0, reelS2: 0, periodes: new Set() };
            const a = acc[siteCourant];
            for (let col = colSite; col <= Math.min(range.e.c, 200); col++) {
                const raw = getStr(sheet, i, col).toLowerCase();
                if (raw.includes('au') && raw.includes('/') && (raw.includes('25') || raw.includes('2025'))) {
                    const montant = getVal(sheet, i, col + 3);
                    const cle = raw + '_' + montant;
                    if (a.periodes.has(cle)) { col += 3; continue; }
                    a.periodes.add(cle);
                    const conso = getVal(sheet, i, col + 2);
                    if (montant < 100000 && montant > 0) {
                        const parts = raw.split('/');
                        let mois = 0;
                        if (parts.length > 1) mois = parseInt(parts[1].replace(/[^0-9]/g, '')) || 0;
                        a.totalConso += conso; a.totalReel += montant;
                        if (mois > 0 && mois <= 6) { a.consoS1 += conso; a.reelS1 += montant; }
                        else { a.consoS2 += conso; a.reelS2 += montant; }
                    }
                    col += 3;
                }
            }
        }
        // Convertir Set en undefined pour JSON
        for (const k in acc) delete acc[k].periodes;
        return acc;
    }

    const gazAcc  = agreger(wb.Sheets['CONSO GAZ'],  2, '2025');
    const elecAcc = agreger(wb.Sheets['CONSO ELEC'], 5, '2025');

    for (const [site, a] of Object.entries(gazAcc)) {
        if (a.totalConso > 0 || a.totalReel > 0) {
            fluides.gaz.push({ site, conso: a.totalConso, reel: a.totalReel, consoS1: a.consoS1, consoS2: a.consoS2, reelS1: a.reelS1, reelS2: a.reelS2 });
        }
    }
    for (const [site, a] of Object.entries(elecAcc)) {
        if (a.totalConso > 0 || a.totalReel > 0) {
            fluides.elec.push({ site, conso: a.totalConso, reel: a.totalReel, consoS1: a.consoS1, consoS2: a.consoS2, reelS1: a.reelS1, reelS2: a.reelS2 });
        }
    }
    console.log(`  → Gaz: ${fluides.gaz.length} sites, Elec: ${fluides.elec.length} sites`);

    const destFluides = path.join(RESOURCES, 'fluides.json');
    fs.writeFileSync(destFluides, JSON.stringify(fluides, null, 2));
    console.log(`✅ fluides.json écrit`);

    // --- SIMULATION (onglet "CALC DEP(4)") ---
    const sheetSim = wb.Sheets['CALC DEP(4)'];
    const simulation = {
        restauration: [],
        totalEnfants: 0,
        depensesReelles: {},
        depensesAccueilLoisirs: {},
        depensesEtudesSurveillees: {},
        depensesEspaceAdos: {},
        depensesSejours: {},
        depensesPeriscolaire: {}
    };

    if (sheetSim) {
        // Lignes 7 à 17 (index 6 à 16)
        for (let i = 6; i <= 16; i++) {
            const codeTranche = getStr(sheetSim, i, 1) || getStr(sheetSim, i, 0);
            const labelTranche = getStr(sheetSim, i, 0);
            if (!codeTranche) continue;
            simulation.restauration.push({
                tranche: labelTranche,
                codeTranche,
                prixFacture: getVal(sheetSim, i, 2),
                nombreEnfants: getVal(sheetSim, i, 3),
                coutMoyen: getVal(sheetSim, i, 4),
                depenseAnnuelle: getVal(sheetSim, i, 5),
                recetteAnnuelle: getVal(sheetSim, i, 6),
                ecart: getVal(sheetSim, i, 7),
                tauxCouverture: getVal(sheetSim, i, 8)
            });
        }
        // Total enfants: ligne 17 (index 16), col 3
        simulation.totalEnfants = getVal(sheetSim, 16, 3);

        // Dépenses réelles restauration: ligne 34 (index 33)
        simulation.depensesReelles = {
            'Scolarest (prestations)': getVal(sheetSim, 33, 2),
            'Personnel': getVal(sheetSim, 33, 3),
            'Alimentation': getVal(sheetSim, 33, 4),
            'Eau': getVal(sheetSim, 33, 5),
            'Electricite': getVal(sheetSim, 33, 6),
            'Gaz': getVal(sheetSim, 33, 7),
            'TOTAL': getVal(sheetSim, 33, 8)
        };

        // Accueil de Loisirs: ligne 46 (index 45)
        const labelsLoisirs = ['Personnel','Materiel','Fournitures pedagogiques','Materiel sportif','Prestations (spectacles)','Alimentation','Transport',"Droits d'entree",'Electricite','Eau','Restauration','Autres'];
        const colsLoisirs = [2,3,4,5,6,7,8,9,10,11,12,13];
        simulation.depensesAccueilLoisirs = {};
        labelsLoisirs.forEach((l,i) => { const v = getVal(sheetSim, 45, colsLoisirs[i]); if (v) simulation.depensesAccueilLoisirs[l] = v; });
        simulation.depensesAccueilLoisirs['TOTAL'] = getVal(sheetSim, 45, 15);

        // Etudes: ligne 60 (index 59)
        simulation.depensesEtudesSurveillees = {};
        [['Personnel',2],['Fournitures scolaires',3],['Materiel',4]].forEach(([l,c]) => {
            const v = getVal(sheetSim, 59, c); if (v) simulation.depensesEtudesSurveillees[l] = v;
        });
        simulation.depensesEtudesSurveillees['TOTAL'] = getVal(sheetSim, 59, 5);

        // Ados: ligne 75 (index 74)
        const labelsAdos = ['Personnel','Electricite','Gaz','Eau','Materiel','Fournitures','Petit materiel','Alimentation','Autres','Transport',"Droits d'entree"];
        simulation.depensesEspaceAdos = {};
        labelsAdos.forEach((l,i) => { const v = getVal(sheetSim, 74, i+2); if (v) simulation.depensesEspaceAdos[l] = v; });
        simulation.depensesEspaceAdos['TOTAL'] = getVal(sheetSim, 74, 13);

        // Sejours: ligne 95 (index 94)
        const labelsSejours = ['Personnel','Transport (bus/mini bus)','Peage/Parking','Hebergement (centre vacances)','Restauration','Materiel','Fournitures','Autres'];
        simulation.depensesSejours = {};
        labelsSejours.forEach((l,i) => { const v = getVal(sheetSim, 94, i+2); if (v) simulation.depensesSejours[l] = v; });
        simulation.depensesSejours['TOTAL'] = getVal(sheetSim, 94, 10);

        // Periscolaire: ligne 112 (index 111)
        simulation.depensesPeriscolaire = {};
        [['Personnel',2],['Fournitures',3],['Alimentation',4],['Eau',5],['Electricite',6],['Gaz',7]].forEach(([l,c]) => {
            const v = getVal(sheetSim, 111, c); if (v) simulation.depensesPeriscolaire[l] = v;
        });
        simulation.depensesPeriscolaire['TOTAL'] = getVal(sheetSim, 111, 8);

        console.log(`  → Simulation restauration: ${simulation.restauration.length} tranches`);
    }

    const destSim = path.join(RESOURCES, 'simulation.json');
    fs.writeFileSync(destSim, JSON.stringify(simulation, null, 2));
    console.log(`✅ simulation.json écrit`);
}

// ============================================================
// 3. Depenses recettes nf.xlsx  →  recettes_source_a.json
// ============================================================
function convertirRecettesSourceA() {
    const wb = lireExcel('Depenses recettes nf.xlsx');
    const sheet = wb.Sheets['recettes'];
    if (!sheet) { console.error('Onglet "recettes" introuvable dans Depenses recettes nf.xlsx'); return; }

    const range = XLSX.utils.decode_range(sheet['!ref'] || 'A1');
    let rowStart = -1, colStart = -1;

    outer:
    for (let r = 0; r < Math.min(50, range.e.r); r++) {
        for (let c = 0; c < Math.min(10, range.e.c); c++) {
            const v = getStr(sheet, r, c).toLowerCase();
            if (v.includes('tableau') && v.includes('recettes')) {
                rowStart = r; colStart = c;
                break outer;
            }
        }
    }

    const recettes = {};
    if (rowStart >= 0) {
        // La ligne de total est 4 lignes plus bas
        const rowTotal = rowStart + 4;
        recettes['Restauration']        = getVal(sheet, rowTotal, colStart + 1);
        recettes['Accueil de Loisirs']  = getVal(sheet, rowTotal, colStart + 2);
        recettes['Accueil periscolaire']= getVal(sheet, rowTotal, colStart + 3);
        recettes['Etudes surveillees']  = getVal(sheet, rowTotal, colStart + 4);
        recettes['Espace Ados']         = getVal(sheet, rowTotal, colStart + 5);
        recettes['Sejours']             = getVal(sheet, rowTotal, colStart + 6) + getVal(sheet, rowTotal, colStart + 7);
    }

    const dest = path.join(RESOURCES, 'recettes_source_a.json');
    fs.writeFileSync(dest, JSON.stringify(recettes, null, 2));
    console.log(`✅ recettes_source_a.json écrit (${Object.keys(recettes).length} pôles)`);
}

// ============================================================
// 4. VF_REC_DEP.xlsx  →  vf_rec_dep.json
// ============================================================
function convertirVfRecDep() {
    // Ce fichier peut ne pas exister, pas bloquant
    const chemin = path.join(RESOURCES, 'VF_REC_DEP.xlsx');
    if (!fs.existsSync(chemin)) {
        console.log(`⚠️  VF_REC_DEP.xlsx absent, vf_rec_dep.json non généré`);
        // Créer un fichier vide pour que l'application ne plante pas
        fs.writeFileSync(path.join(RESOURCES, 'vf_rec_dep.json'), JSON.stringify({ poles: [], recettes: {} }, null, 2));
        return;
    }

    console.log(`📂 Lecture: VF_REC_DEP.xlsx`);
    const wb = XLSX.readFile(chemin, { cellFormula: false });

    const result = { poles: [], recettes: {} };

    // Onglet DEP
    const dep = wb.Sheets['DEP'];
    if (dep) {
        function lireCharges(sheet, rowFrom, rowTo, colDesc, colTotal) {
            const charges = {};
            for (let r = rowFrom; r <= rowTo; r++) {
                const desc = getStr(sheet, r, colDesc);
                if (!desc) continue;
                const montant = getVal(sheet, r, colTotal);
                if (montant > 0) charges[desc] = montant;
            }
            return charges;
        }

        // RE
        const totalRE = getVal(dep, 20, 21);
        const chargesRE = lireCharges(dep, 3, 19, 0, 21);
        const effectifRE = getVal(dep, 21, 2);
        result.poles.push({ nom: 'Restauration', total: totalRE, effectif: effectifRE, charges: chargesRE });

        // SC
        const totalSC = getVal(dep, 45, 10);
        const chargesSC = lireCharges(dep, 29, 44, 0, 10);
        const effectifSC = getVal(dep, 46, 10);
        result.poles.push({ nom: 'Scolaire', total: totalSC, effectif: effectifSC, charges: chargesSC });

        // CL
        const totalCL = getVal(dep, 64, 6);
        const chargesCL = lireCharges(dep, 52, 63, 0, 6);
        const effectifCL = getVal(dep, 65, 6);
        result.poles.push({ nom: 'Accueil de Loisirs', total: totalCL, effectif: effectifCL, charges: chargesCL });
    }

    // Onglet REC
    const rec = wb.Sheets['REC'];
    if (rec) {
        const range = XLSX.utils.decode_range(rec['!ref'] || 'A1');
        let rowStart = -1, colStart = -1;
        outer:
        for (let r = 0; r < Math.min(30, range.e.r); r++) {
            for (let c = 0; c < Math.min(15, range.e.c); c++) {
                const v = getStr(rec, r, c).toLowerCase();
                if (v.includes('tableau') && v.includes('recettes')) {
                    rowStart = r; colStart = c;
                    break outer;
                }
            }
        }
        if (rowStart >= 0) {
            const rowTotal = rowStart + 4;
            result.recettes['Restauration']       = getVal(rec, rowTotal, colStart + 1);
            result.recettes['Accueil de Loisirs'] = getVal(rec, rowTotal, colStart + 2);
        }
        // RESTSCOLL ligne 8 (index 7) col C (index 2)
        result.recettes['Scolaire'] = getVal(rec, 7, 2);
    }

    const dest = path.join(RESOURCES, 'vf_rec_dep.json');
    fs.writeFileSync(dest, JSON.stringify(result, null, 2));
    console.log(`✅ vf_rec_dep.json écrit (${result.poles.length} pôles)`);
}

// ============================================================
// MAIN
// ============================================================
console.log('🚀 Début de la conversion Excel → JSON\n');

try { convertirSynthese(); } catch(e) { console.error('Erreur synthese:', e.message); }
try { convertirCalcDep4(); } catch(e) { console.error('Erreur calcDep4:', e.message); }
try { convertirRecettesSourceA(); } catch(e) { console.error('Erreur recettesSourceA:', e.message); }
try { convertirVfRecDep(); } catch(e) { console.error('Erreur vfRecDep:', e.message); }

console.log('\n✨ Conversion terminée ! Les fichiers JSON sont dans src/main/resources/');
console.log('💡 Vous pouvez maintenant déployer sur Railway sans problème mémoire.');
