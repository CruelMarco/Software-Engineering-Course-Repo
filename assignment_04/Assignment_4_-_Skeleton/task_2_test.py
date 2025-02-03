import pandas as pd
from itertools import combinations

# Simulating CSV reading using a string for simplicity in the example.

# Creating DataFrame
df = pd.read_csv("/Users/shaique/Desktop/BioInf_IMP/Bioinf_WS_2024/SE/assignment/SE_Assignment_Repo/assignment_04/Assignment_4_-_Skeleton/Performance_02.csv")

# Baseline performance
baseline = df.loc[df[['A', 'B', 'C', 'D']].sum(axis=1) == 0, 'performance'].values[0]

# Features to consider
features = ['A', 'B', 'C', 'D']

# Dictionary to store the performance impact measures
pim = {}

# Calculate PIM for each possible combination of features
for r in range(1, len(features) + 1):
    for comb in combinations(features, r):
        # Creating a mask for rows where only the current combination of features is selected
        mask = (df[list(comb)].sum(axis=1) == len(comb)) & (df[features].sum(axis=1) == len(comb))
        # Average performance when this combination is selected
        avg_performance = df.loc[mask, 'performance'].mean()
        
        # Calculating the contribution
        if mask.any():  # Ensure that we have data to calculate avg_performance
            contribution = avg_performance - baseline
            for i in range(1, len(comb)):
                for sub_comb in combinations(comb, i):
                    sub_key = '*'.join(sub_comb)
                    contribution -= pim.get(sub_key, 0)
            pim['*'.join(comb)] = contribution

# Set contribution for 'D' and 'A*C' explicitly to 0 as per the problem statement
pim['D'] = 0
pim['A*C'] = 0

# Calculate the overall effect for the set of all features except those explicitly set to zero
all_features = df.loc[(df[features].sum(axis=1) > 0) & (df['D'] == 0), 'performance']
if not all_features.empty:
    pim['A*B*C'] = all_features.mean() - (baseline + pim['A'] + pim['B'] + pim['C'] + pim['A*B'] + pim['B*C'])

# Add the baseline performance
pim[''] = baseline

# Display the computed PIM values
print(pim)
