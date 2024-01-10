import axios from "axios";
import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { API_URL } from '../config';
import BackgroundWiki from "./BackgroundWiki";

function ModifierCategorie() {
    const { id, oldCategoryName } = useParams();
    const [newCategoryName, setNewCategoryName] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const navigate = useNavigate();

    const handleChangeName = (e) => {
        setNewCategoryName(e.target.value);
    };

    const handleRetourClick = () => {
        navigate(-1);
    };

    function handleModifyCategory() {
        const requestData = {
            id: parseInt(id),
            categories: oldCategoryName
        };
        console.log(requestData);
        if (newCategoryName.trim().length === 0) {
            setErrorMessage("Veuillez remplir le champ");
        } else {
            setErrorMessage("");
            axios.put( API_URL+'/modify/category/' + newCategoryName, requestData).then((response) => {
                console.log(response);
            }).catch((error) => {
                console.error("Erreur lors de la modification de la categorie :", error);
            });
            navigate(-1);
        }
    }

    return (
        <div>
            <BackgroundWiki id={id} />
            <p> Ancien nom de la catégorie : {oldCategoryName} </p>
            <input type="text" placeholder="Nouveau nom categorie" onChange={handleChangeName} value={newCategoryName} />
            <button onClick={handleModifyCategory}> Modifier </button>
            <button style={{ cursor: 'pointer' }} onClick={handleRetourClick}>Retour</button>
            <p>{errorMessage}</p>
        </div>
    );
}

export default ModifierCategorie;