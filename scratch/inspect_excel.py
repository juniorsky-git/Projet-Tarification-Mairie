import pandas as pd
import sys

file_path = "Donnees/Autres/CALC DEP.xlsx"
try:
    # Read sheet index 8 (0-indexed)
    df = pd.read_excel(file_path, sheet_name=8, header=None)
    
    # Lines 42 to 46 (1-indexed) are rows 41 to 45 (0-indexed)
    rows_to_check = range(40, 50) # A bit wider to be safe
    
    print(f"File: {file_path}")
    print(f"Sheet index: 8")
    print("-" * 50)
    
    for i in rows_to_check:
        if i >= len(df):
            break
        row = df.iloc[i]
        # Column R is index 17
        val_r = row[17] if len(row) > 17 else "N/A"
        # Print the whole row content to see where keywords are
        row_content = " | ".join([str(val) for val in row])
        print(f"Row {i+1}: {row_content}")
        print(f"  -> Column R (index 17): {val_r}")
        print("-" * 50)

except Exception as e:
    print(f"Error: {e}")
    sys.exit(1)
