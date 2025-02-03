# This is the python template for Assignment 04.
# - You must use this template.
# - You must not add any additional imports.
# - You must not change any signatures of the methods, only edit the sections that are clearly marked for modification
# - You are allowed to add helper functions.
# - The return value of every function has to be in the right format, otherwise this is a desk reject.
# - Plagiarism leads to failing the assignment!

import itertools
import pandas as pd
import typing as tp


def get_name() -> str:
    """
    Returns the name of the student submitting this assignment

    @return: Your name
    """

    
    return "Mohammad Shaique Solanki"


def get_matriculation_number() -> int:
    """
    Returns the matriculation number of the student submitting this assignment

    @return: Your matriculation number
    """
    return 7062950


# Helper functions for CART

def mse(csv_file):
    
    perf_column = csv_file["performance"]
    
    mean_perf_error_list = [(i - perf_column.mean())**2 for i in perf_column]
    
    mse_error = sum(mean_perf_error_list)
    
    return mse_error


def feature_with_error_extract(csv_file):
    
    errors_acc = []
    
    for i in csv_file.columns[1:-1]:  
        
        error_1 = mse(csv_file=csv_file[csv_file[i] == 1])  
        
        error_0 = mse(csv_file=csv_file[csv_file[i] == 0])  
        
        total_error = error_1 + error_0  
        
        errors_acc.append((total_error, i))

    min_error = min(errors_acc, key=lambda x: x[0])[0]

    min_error_columns = [(column, errors_acc) for errors_acc, column in errors_acc if errors_acc == min_error]

    min_error_columns_sorted = sorted(min_error_columns, key=lambda x: x[0])
    
    feature, error = min_error_columns_sorted[0]

    return feature, error
    
def cart_build(csv_file , name = "X"):
        
    target_column = csv_file.columns[-1]

    if csv_file[target_column].nunique() == 1 or csv_file.empty:
        
        return {
            
            "name": name,
            
            "mean": csv_file[target_column].mean(),
            
            "split_by_feature": None,
            
            "error_of_split": None,
            
            "successor_left": None,
            
            "successor_right": None
            
        }

    csv_file = csv_file.drop(csv_file.nunique()[csv_file.nunique() == 1].index, axis=1)

    split_feature = feature_with_error_extract(csv_file)[0]  
    
    error = feature_with_error_extract(csv_file)[1]

    return {

        "name": name,
        
        "mean": csv_file[target_column].mean(),
        
        "split_by_feature": split_feature,
        
        "error_of_split": error,
        
        "successor_left": cart_build(csv_file=csv_file[csv_file[split_feature] == 1], name=name + 'L'),
        
        "successor_right": cart_build(csv_file=csv_file[csv_file[split_feature] == 0], name=name + 'R')
        
    }


        

# End of helper functions
def get_cart(sample_set_csv: str) -> tp.Dict[str, tp.Union[str, float, tp.Dict]]:
    """
    Calculate the CART based on a set of measurements

    @param sample_set_csv: Path to a CSV file to use as a basis for the CART
    @return: The CART in a format as specified in the "General Information" section in the Assignment sheet
    """
    # The sample_set_csv is a file path to a csv data, this can be imported into a dataframe    
    
    df = pd.read_csv(sample_set_csv)
    # TODO: Write your code here. And change the return.
    
    
    # return {"name": "X", "mean": 123.4, "split_by_feature": "rar", "error_of_split": 4.2,
    #         "successor_left": None, "successor_right": None}
    
    cart = cart_build(df)
    

    return cart

#print(get_cart)
##############################################
# Task 2: Create a Performance Influence Model
# Write your helper functions here, if needed




# End of helper functions
def get_pim(sample_set_csv: str) -> tp.Dict[str, float]:
    """
    Calculate the Performance Influence Model (PIM) of a system based on a set of performance measurements

    @param sample_set_csv: Path to a CSV file to use as a basis for the PIM
    @return: PIM for the given set of measurements
    """
    # The sample_set_csv is a file path to a csv data, this can be imported into a dataframe
    df = pd.read_csv(sample_set_csv)
    # TODO: Write your code here. And change the return.
    
    baseline_row = df[(df['A'] == 0) & (df['B'] == 0) & (df['C'] == 0) & (df['D'] == 0)]
    
    baseline = baseline_row['performance'].values[0]

    pim_dict = {"": baseline}

    feature_sets = []

    for idx, row in df.iterrows():
        
        if idx == 0:
            
            continue
        active = [col for col in ['A', 'B', 'C', 'D'] if row[col] == 1]
        
        if not active:
            
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

    pim = pim_dict
    
    
    return pim


# Task 3a: Predicted performance

# Write your helper functions here, if needed

# End of helper functions
def get_performance(cart: tp.Dict[str, tp.Union[str, float, tp.Dict]], configuration: tp.Set[str]) -> float:
    """
    Calculate the performance of a given configuration for a specific CART

    @param cart: CART to use for the performance prediction
    @param configuration: The configuration for which to predict the performance
    @return: The expected performance for a specific configuration, according to a given CART
    """
    # TODO: Write your code here. And change the return.
    
    cart_3a = cart
    
    feature = cart_3a['split_by_feature']
    
    if feature in configuration:
        
        cart_3a = cart_3a['successor_left']
        
    else:
        
        cart_3a = cart_3a['successor_right']
        
    
    prediction = cart_3a['mean']
    
    return prediction


# Task 3b: Calculate the error rate
# Write your helper functions here, if needed

# End of helper functions
def get_error_rate(cart: tp.Dict[str, tp.Union[str, float, tp.Dict]]
                   , sample_set_csv: str) -> float:
    """
    Calculate the error rate of a CART

    @param cart: A CART of a software system
    @param sample_set_csv: Path to a CSV file with a set of performance measurements.
    @return: The error rate of the CART with respect to the actual performance measurements
    """
    # The sample_set_csv is a file path to a csv data, this can be imported into a dataframe
    df = pd.read_csv(sample_set_csv)
    # TODO: Write your code here. And change the return.
    return 42


# Task 3c: Generate optimal configuration
# Write your helper functions here, if needed

# End of helper functions
def get_optimal_configuration(pim: tp.Dict[str, float]
                              , feature_model : tp.Dict[str, tp.Union[str,tp.Dict, None]]
                              , partial_configuration: tp.Set[str])\
        -> tp.Tuple[tp.Set[str], float]:
    """
    Find the optimal full configuration according to a specific PIM while adhering to a specific partial configuration.
    Assumes that the performance is the cost of a system. Thus, the optimal configuration is the cheapest one.

    @param pim: The PIM to use as a basis for performance prediction
    @param feature_model: The feature model of the system to find the best configuration for.
    @param partial_configuration: Partial configuration to find the optimal configuration for.
    @return: A tuple which contains the set of selected features as its first entry and the corresponding performance value as its second entry
    """
    # TODO: Write your code here. And change the return.
    return {"rar", "timestamp"}, 42
