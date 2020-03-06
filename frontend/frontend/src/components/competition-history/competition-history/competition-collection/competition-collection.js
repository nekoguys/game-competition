import React from "react";

class CompetitionCollectionElement extends React.Component {
    render() {
        return <div className={"competition-collection-element"}>
            <div className={""}/>
        </div>
    }
}

class CompetitionCollection extends React.Component {
    constructor(props) {
        super(props)
    }

    render() {

        const {items} = this.props;

        const elems = items.map(item => {
            return <div key={item.pin}>
                <CompetitionCollectionElement
                    name={item.name}
                    status={item.status}
                    id={item.id}/>
            </div>
        });

        return (
            <div className="competition-collection">
                {elems}
            </div>
        )


    }
}

export default CompetitionCollection;