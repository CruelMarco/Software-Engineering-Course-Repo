import pandas as pd
import itertools

df = pd.read_csv('Performance_02.csv')

baseline_row = df[(df['A'] == 0) & (df['B'] == 0) & (df['C'] == 0) & (df['D'] == 0)]
baseline = baseline_row['performance'].values[0]

pim_dict = {"": baseline}

feature_sets = []

for idx, row in df.iterrows():
    if idx == 0:
        continue
    active = [col for col in ['A', 'B', 'C', 'D'] if row[col] == 1]
    if not active or active == ['D']:
        continue
    active_sorted = sorted(active)
    feature_sets.append((active_sorted, row['performance']))

feature_sets.sort(key=lambda x: len(x[0]))

for features, perf in feature_sets:
    key = "*".join(features)
    n = len(features)
    sum_pim = 0
    
    for k in range(1, n):
        for subset in itertools.combinations(features, k):
            subset_key = "*".join(sorted(subset))
            if len(subset) == 1 and subset[0] == 'D':
                continue
            sum_pim += pim_dict.get(subset_key, 0)
    
    current_pim = perf - (baseline + sum_pim)
    pim_dict[key] = current_pim

test_pim = pim_dict
print(test_pim)
